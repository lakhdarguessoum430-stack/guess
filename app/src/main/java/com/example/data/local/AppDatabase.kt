package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyLogEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        ExpenseEntity::class,
        TaskEntity::class,
        DailyLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun taskDao(): TaskDao
    abstract fun dailyLogDao(): DailyLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "guessoum_construction_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate database with realistic construction starter data
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            val p1Id = db.projectDao().insertProject(
                ProjectEntity(
                    name = "مشروع إنجاز 40 سكن ترقوي",
                    location = "الجزائر العاصمة - باب الزوار",
                    budget = 45000000.0,
                    status = ProjectEntity.STATUS_IN_PROGRESS,
                    startDate = now - 35 * dayMs,
                    clientName = "ديوان الترقية العقارية OPGI",
                    supervisorName = "المهندس أرزقي قسوم",
                    notes = "أعمال الهيكل الخرساني في الطابق الثالث جارية بوتيرة جيدة."
                )
            )

            val p2Id = db.projectDao().insertProject(
                ProjectEntity(
                    name = "بناء مجمع مدرسي ونصف داخلية",
                    location = "وهران - بئر الجير",
                    budget = 22000000.0,
                    status = ProjectEntity.STATUS_IN_PROGRESS,
                    startDate = now - 15 * dayMs,
                    clientName = "مديرية التجهيزات العمومية DLEP",
                    supervisorName = "المهندس فريد مرابط",
                    notes = "تم إنهاء أعمال الحفر والأساسات والشروع في الأعمدة."
                )
            )

            val p3Id = db.projectDao().insertProject(
                ProjectEntity(
                    name = "تهيئة وتزفيت الطريق الرابط بين البلديات",
                    location = "قسنطينة - عين السمارة",
                    budget = 12500000.0,
                    status = ProjectEntity.STATUS_COMPLETED,
                    startDate = now - 90 * dayMs,
                    clientName = "مديرية الأشغال العمومية DTP",
                    supervisorName = "مكتب الدراسات الإنشائية",
                    notes = "تم تسليم المشروع بنجاح وشهادة المطابقة معتمدة."
                )
            )

            val p4Id = db.projectDao().insertProject(
                ProjectEntity(
                    name = "مركز خدمات ومخازن لوجستية",
                    location = "سطيف - المنطقة الصناعية",
                    budget = 35000000.0,
                    status = ProjectEntity.STATUS_PAUSED,
                    startDate = now - 60 * dayMs,
                    clientName = "مستثمر خاص",
                    supervisorName = "م. رشيد بلقاسم",
                    notes = "توقف مؤقت بانتظار تعديل مخططات شبكة الحماية من الحرائق."
                )
            )

            // Seed expenses for Project 1
            db.expenseDao().insertExpenses(
                listOf(
                    ExpenseEntity(
                        projectId = p1Id,
                        title = "شراء 25 طن حديد تسليح FeE500",
                        amount = 4500000.0,
                        category = ExpenseEntity.CAT_MATERIALS,
                        date = now - 30 * dayMs,
                        notes = "فاتورة مورد الحديد مجمع الحجار"
                    ),
                    ExpenseEntity(
                        projectId = p1Id,
                        title = "خرسانة جاهزة B25 صب الأعمدة",
                        amount = 3200000.0,
                        category = ExpenseEntity.CAT_MATERIALS,
                        date = now - 20 * dayMs,
                        notes = "خلاطة مركزية 40 متر مكعب"
                    ),
                    ExpenseEntity(
                        projectId = p1Id,
                        title = "أجور عمال البناء والحدادة (دفعة أسبوعين)",
                        amount = 950000.0,
                        category = ExpenseEntity.CAT_LABOR,
                        date = now - 7 * dayMs,
                        notes = "فريق 14 عامل وفني"
                    ),
                    ExpenseEntity(
                        projectId = p1Id,
                        title = "كراء مضخة ورافعة خرسانة برجية",
                        amount = 650000.0,
                        category = ExpenseEntity.CAT_EQUIPMENT,
                        date = now - 12 * dayMs,
                        notes = "عقد شهري"
                    )
                )
            )

            // Seed tasks for Project 1
            db.taskDao().insertTasks(
                listOf(
                    TaskEntity(projectId = p1Id, title = "أعمال الحفر وتطهير الورشة", isCompleted = true, milestonePhase = "الأساسات"),
                    TaskEntity(projectId = p1Id, title = "صب القواعد الخرسانية العادية والمسلحة", isCompleted = true, milestonePhase = "الأساسات"),
                    TaskEntity(projectId = p1Id, title = "صب أعمدة وأسقف الطابق الأرضي", isCompleted = true, milestonePhase = "الهيكل الخرساني"),
                    TaskEntity(projectId = p1Id, title = "أعمدة وسقف الطابق الأول والثاني", isCompleted = true, milestonePhase = "الهيكل الخرساني"),
                    TaskEntity(projectId = p1Id, title = "صب خرسانة الطابق الثالث", isCompleted = false, milestonePhase = "الهيكل الخرساني"),
                    TaskEntity(projectId = p1Id, title = "أعمال البناء بالطوب الإسمنتي والعوازل", isCompleted = false, milestonePhase = "التشطيبات"),
                    TaskEntity(projectId = p1Id, title = "تمديدات شبكة الكهرباء والسباكة", isCompleted = false, milestonePhase = "التشطيبات")
                )
            )

            // Seed daily logs for Project 1
            db.dailyLogDao().insertLog(
                DailyLogEntity(
                    projectId = p1Id,
                    date = now - 1 * dayMs,
                    workersCount = 16,
                    weather = "مشمس ومثالي للصب",
                    workSummary = "استكمال تركيب نجارة سقف الطابق الثالث والتسليح الحديدي.",
                    supervisorNote = "فحص المسافات وأقطار حديد التسليح وفق المخطط التنفيذي."
                )
            )
        }
    }
}
