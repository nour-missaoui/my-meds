package com.tunmed.pillswaterreminder.data.repository

import com.tunmed.pillswaterreminder.data.local.dao.MedicationDao
import com.tunmed.pillswaterreminder.data.mapper.toDomain
import com.tunmed.pillswaterreminder.data.mapper.toEntity
import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.model.ScheduledMedication
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class MedicationRepositoryImpl(
    private val dao: MedicationDao,
) : MedicationRepository {

    override fun observeMedications(): Flow<List<Medication>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeMedication(id: Long): Flow<Medication?> =
        dao.observeById(id).map { entity -> entity?.toDomain() }

    override suspend fun getCurrentSchedule(medicationId: Long): MedSchedule? =
        dao.getCurrentSchedule(medicationId)?.toDomain()

    override suspend fun getScheduledMedications(): List<ScheduledMedication> {
        val schedulesByMedication = dao.getActiveSchedules().associateBy { it.medicationId }
        return dao.getActive().mapNotNull { medication ->
            schedulesByMedication[medication.id]?.let { schedule ->
                ScheduledMedication(medication = medication.toDomain(), schedule = schedule.toDomain())
            }
        }
    }

    override fun observeScheduledMedications(): Flow<List<ScheduledMedication>> =
        combine(dao.observeAll(), dao.observeOpenSchedules()) { medications, schedules ->
            val scheduleByMedication = schedules.associateBy { it.medicationId }
            medications
                .filter { it.status == MedicationStatus.ACTIVE.name }
                .mapNotNull { medication ->
                    scheduleByMedication[medication.id]?.let { schedule ->
                        ScheduledMedication(medication = medication.toDomain(), schedule = schedule.toDomain())
                    }
                }
        }

    override fun observeCurrentSchedules(): Flow<List<MedSchedule>> =
        dao.observeOpenSchedules().map { list -> list.map { it.toDomain() } }

    override suspend fun save(medication: Medication, schedule: MedSchedule): Long {
        val id = if (medication.id == 0L) {
            dao.insert(medication.toEntity())
        } else {
            dao.update(medication.toEntity())
            medication.id
        }
        dao.getCurrentSchedule(id)?.let { current ->
            dao.closeSchedule(current.id, schedule.validFrom.toString())
        }
        dao.insertSchedule(schedule.copy(id = 0, medicationId = id, validTo = null).toEntity())
        return id
    }

    override suspend fun setStatus(id: Long, status: MedicationStatus) {
        dao.setStatus(id, status.name)
    }

    override suspend fun archiveEndedBefore(date: LocalDate) {
        dao.archiveEndedBefore(date.toString())
    }

    override suspend fun restore(id: Long, today: LocalDate) {
        dao.restore(id, today.toString())
    }
}
