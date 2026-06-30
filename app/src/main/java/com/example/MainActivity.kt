package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SlateBg
                ) { innerPadding ->
                    GodotProjectHubScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Data models for Godot Files
data class GodotFileItem(
    val virtualPath: String,
    val fileName: String,
    val category: String, // Autoload, Resource, Scene, Script, Core
    val farsiTitle: String,
    val farsiDescription: String,
    val propertiesList: List<String>,
    val methodsList: List<String>,
    val codeContent: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GodotProjectHubScreen(modifier: Modifier = Modifier) {
    var activeTab by remember { mutableStateOf("sandbox") } // "explorer" or "sandbox"
    var selectedFileIndex by remember { mutableStateOf(1) } // Default to EventBus index
    var searchQuery by remember { mutableStateOf("") }
    
    // Sandbox states
    var charName by remember { mutableStateOf("Survivor Knight") }
    var charMaxHp by remember { mutableFloatStateOf(120f) }
    var charSpeed by remember { mutableFloatStateOf(190f) }
    var charDefense by remember { mutableFloatStateOf(6f) }
    
    var weapName by remember { mutableStateOf("Thunder Orb") }
    var weapDamage by remember { mutableFloatStateOf(20f) }
    var weapFireRate by remember { mutableFloatStateOf(1.8f) }
    var weapProjSpeed by remember { mutableFloatStateOf(320f) }

    var waveNum by remember { mutableIntStateOf(1) }
    var waveInterval by remember { mutableFloatStateOf(2.5f) }
    var waveDamage by remember { mutableFloatStateOf(12f) }
    var waveEnemyHp by remember { mutableFloatStateOf(40f) }
    var waveEnemyCount by remember { mutableIntStateOf(15) }

    // XP & Level up simulation states
    var simXp by remember { mutableFloatStateOf(0f) }
    var simLevel by remember { mutableIntStateOf(1) }
    var showUpgradeDialog by remember { mutableStateOf(false) }

    // Local Save & Game Over simulation states
    var survivalTime by remember { mutableFloatStateOf(154f) }
    var totalCoinsCollected by remember { mutableIntStateOf(210) }
    var bestSurvivalTime by remember { mutableFloatStateOf(320f) }
    var unlockedItemsList by remember { mutableStateOf(listOf("sword", "thunder_orb")) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Initialize list of Godot Project Files
    val godotFiles = remember {
        listOf(
            GodotFileItem(
                virtualPath = "res://project.godot",
                fileName = "project.godot",
                category = "Core",
                farsiTitle = "تنظیمات کلیدی پروژه گودو ۴",
                farsiDescription = "تنظیمات اساسی پروژه شامل رزولوشن عمودی مخصوص موبایل (۷۲۰ در ۱۲۸۰)، نوع رندرینگ، و معرفی کلاس‌های سراسری (Autoload Singletons) به عنوان موتورهای هدایت بازی.",
                propertiesList = listOf(
                    "config/name = \"Bullet Heaven Mobile\"",
                    "run/main_scene = \"res://scenes/levels/main_level.tscn\"",
                    "window/size/viewport_width = 720",
                    "window/size/viewport_height = 1280",
                    "window/stretch/mode = \"canvas_items\""
                ),
                methodsList = listOf("Auto-Registers Global EventBus", "Auto-Registers Global GameManager", "Auto-Registers Global SaveManager", "Auto-Registers Global EnemyPool"),
                codeContent = """config_version=5

[application]

config/name="Bullet Heaven Godot 4 Mobile Template"
run/main_scene="res://scenes/levels/main_level.tscn"
config/features=PackedStringArray("4.0", "Forward Plus")

[autoload]

EventBus="*res://autoload/event_bus.gd"
GameManager="*res://autoload/game_manager.gd"
SaveManager="*res://autoload/save_manager.gd"
EnemyPool="*res://autoload/enemy_pool.gd"

[display]

window/size/viewport_width=720
window/size/viewport_height=1280
window/size/window_width_override=360
window/size/window_height_override=640
window/stretch/mode="canvas_items"
window/stretch/aspect="expand"
window/handheld/orientation=1""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://autoload/event_bus.gd",
                fileName = "event_bus.gd",
                category = "Autoload",
                farsiTitle = "اتوبوس سیگنال‌های بازی (EventBus)",
                farsiDescription = "مرکز هدایت پیام‌ها و رویدادهای مستقل بازی. بازیکن، دشمنان، سلاح‌ها و رابط کاربری مستقیماً به هم متصل نمی‌شوند، بلکه فقط کدهای خود را به این رویدادخوان ارسال کرده یا از آن گوش می‌دهند. بسیار بهینه برای بازی‌های سبک روگ‌لایک شیفت موج‌دار.",
                propertiesList = listOf("بدون نیاز به نمونه‌سازی اولیه به صورت سراسری در دسترس است."),
                methodsList = listOf(
                    "signal player_spawned(player_node)",
                    "signal player_health_changed(current, max)",
                    "signal player_died",
                    "signal enemy_died(enemy, score, xp)",
                    "signal wave_started(wave_number)",
                    "signal game_over(time_survived, final_xp)"
                ),
                codeContent = """extends Node

# -------------------------------------------------------------
# EventBus - اتولود سیگنال‌های عمومی بازی (Event Bus Pattern)
# منطق: این کلاس به عنوان یک مرکز هدایت رویدادها عمل می‌کند تا بخش‌های
# مختلف بازی (بازیکن، دشمنان، سلاح‌ها و رابط کاربری) بدون وابستگی مستقیم
# به یکدیگر با هم ارتباط برقرار کنند. کاملاً مناسب برای بازی‌های Bullet Heaven.
# -------------------------------------------------------------

# سیگنال‌های مربوط به بازیکن
signal player_spawned(player_node: CharacterBody2D)
signal player_health_changed(current_health: float, max_health: float)
signal player_died
signal player_level_up(new_level: int)
signal xp_collected(amount: int, current_xp: int, next_level_xp: int)

# سیگنال‌های مربوط به دشمنان
signal enemy_spawned(enemy_node: CharacterBody2D)
signal enemy_died(enemy_node: CharacterBody2D, score_value: int, xp_value: int)

# سیگنال‌های مربوط به لول و موج‌ها
signal wave_started(wave_number: int)
signal wave_completed(wave_number: int)

# سیگنال‌های مربوط به گیم‌پلی عمومی
signal game_started
signal game_paused(is_paused: bool)
signal game_over(time_survived: float, final_xp: int)""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://autoload/game_manager.gd",
                fileName = "game_manager.gd",
                category = "Autoload",
                farsiTitle = "مدیریت گیم‌پلی و تراز بازی (GameManager)",
                farsiDescription = "هسته کنترل‌کننده محاسبات امتیاز، ارتقاء تراز (XP)، زمان زنده ماندن بازیکن بر حسب ثانیه، متوقف کردن یا پایان بازی و اتصال الگوهای گیم‌پلی با SaveManager آفلاین.",
                propertiesList = listOf(
                    "is_game_active: bool",
                    "current_score: int, current_gold: int",
                    "current_level: int, current_xp: int",
                    "xp_to_next_level: int",
                    "time_elapsed: float"
                ),
                methodsList = listOf(
                    "start_game() -> شروع چرخه گیم‌پلی زنده",
                    "pause_game(is_paused) -> توقف یا ادامه بازی",
                    "add_xp(amount) -> تزریق تجربه و بررسی تراز جدید",
                    "level_up() -> انتقال بازیکن به تراز بالاتر و افزایش سختی ریاضی",
                    "_on_enemy_died(enemy, score, xp) -> دریافت سیگنال دریافت پاداش مرگ دشمن"
                ),
                codeContent = """extends Node

# -------------------------------------------------------------
# GameManager - مدیریت چرخه بازی و محاسبات امتیاز و تراز (XP)
# نقش: مدیریت وضعیت جاری بازی (شروع، توقف، باخت)، رهگیری موج‌ها،
# امتیاز، پول جمع‌آوری شده و تراز بازیکن (Level Up).
# -------------------------------------------------------------

var is_game_active: bool = false
var current_score: int = 0
var current_gold: int = 0
var current_level: int = 1
var current_xp: int = 0
var xp_to_next_level: int = 100
var time_elapsed: float = 0.0

func _ready() -> void:
	# اتصال به رویدادهای عمومی در EventBus
	EventBus.connect("enemy_died", Callable(self, "_on_enemy_died"))
	EventBus.connect("player_died", Callable(self, "_on_player_died"))

func _process(delta: float) -> void:
	if is_game_active:
		time_elapsed += delta

func start_game() -> void:
	is_game_active = true
	current_score = 0
	current_gold = 0
	current_level = 1
	current_xp = 0
	xp_to_next_level = 100
	time_elapsed = 0.0
	EventBus.emit_signal("game_started")

func pause_game(is_paused: bool) -> void:
	get_tree().paused = is_paused
	EventBus.emit_signal("game_paused", is_paused)

func add_xp(amount: int) -> void:
	current_xp += amount
	if current_xp >= xp_to_next_level:
		level_up()
	else:
		EventBus.emit_signal("xp_collected", amount, current_xp, xp_to_next_level)

func level_up() -> void:
	current_xp -= xp_to_next_level
	current_level += 1
	xp_to_next_level = int(xp_to_next_level * 1.5) # افزایش سختی تراز بعدی
	EventBus.emit_signal("player_level_up", current_level)
	EventBus.emit_signal("xp_collected", 0, current_xp, xp_to_next_level)

func _on_enemy_died(_enemy_node: CharacterBody2D, score_value: int, xp_value: int) -> void:
	current_score += score_value
	add_xp(xp_value)

func _on_player_died() -> void:
	is_game_active = false
	# ذخیره زمان زنده ماندن و سکه‌های دریافتی به صورت آفلاین در JSON
	SaveManager.update_best_time(time_elapsed)
	SaveManager.add_coins(current_gold)
	EventBus.emit_signal("game_over", time_elapsed, current_xp)""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://autoload/save_manager.gd",
                fileName = "save_manager.gd",
                category = "Autoload",
                farsiTitle = "مدیریت ذخیره‌سازی آفلاین و رکوردهای محلی (SaveManager)",
                farsiDescription = "مدیریت ذخیره و بارگذاری اطلاعات در پوشه محلی گودو به صورت JSON خوانا (`user://save.json`). ذخیره بهترین زمان بقا (best_survival_time)، سکه‌های جمع‌آوری شده (coins_collected) و لیست آیتم‌ها یا تجهیزات آنلاک شده بازیکن.",
                propertiesList = listOf(
                    "SAVE_PATH = \"user://save.json\"",
                    "best_survival_time: float",
                    "coins_collected: int",
                    "unlocked_items: Array"
                ),
                methodsList = listOf(
                    "save_game() -> تبدیل داده‌ها به رشته فرمت‌دار JSON و نوشتن روی حافظه",
                    "load_game() -> خواندن فایل متنی و پارس نمودن مقادیر با ساختار دیکشنری",
                    "update_best_time(new_time) -> ارزیابی برتری زمان و ثبت رکورد جدید",
                    "add_coins(amount) -> افزودن سکه‌های کسب شده کارزار بقا به موجودی اصلی"
                ),
                codeContent = """extends Node

# -------------------------------------------------------------
# SaveManager - مدیریت آفلاین ذخیره‌سازی داده‌های اصلی با فرمت JSON
# نقش: همگام‌سازی سکه‌ها و بهترین رکوردهای زمانی به صورت بومی
# تحت آدرس بوم‌شناختی user://save.json
# -------------------------------------------------------------

const SAVE_PATH = "user://save.json"

var best_survival_time: float = 0.0
var coins_collected: int = 0
var unlocked_items: Array = ["sword", "thunder_orb"]

func _ready() -> void:
	load_game()

func save_game() -> void:
	var file = FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file:
		var save_data = {
			"best_survival_time": best_survival_time,
			"coins_collected": coins_collected,
			"unlocked_items": unlocked_items
		}
		var json_string = JSON.stringify(save_data, "\t")
		file.store_string(json_string)
		file.close()

func load_game() -> void:
	if FileAccess.file_exists(SAVE_PATH):
		var file = FileAccess.open(SAVE_PATH, FileAccess.READ)
		if file:
			var json_string = file.get_as_text()
			file.close()
			
			var json = JSON.new()
			var parse_err = json.parse(json_string)
			if parse_err == OK:
				var save_data = json.get_data()
				if save_data is Dictionary:
					best_survival_time = save_data.get("best_survival_time", 0.0)
					coins_collected = save_data.get("coins_collected", 0)
					unlocked_items = save_data.get("unlocked_items", ["sword", "thunder_orb"])

func update_best_time(new_time: float) -> void:
	if new_time > best_survival_time:
		best_survival_time = new_time
		save_game()

func add_coins(amount: int) -> void:
	coins_collected += amount
	save_game()""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://resources/character_data.gd",
                fileName = "character_data.gd",
                category = "Resource",
                farsiTitle = "منبع داده سفارشی بازیکن (CharacterData)",
                farsiDescription = "تعریف ساختار داده بازیکن به صورت کلاس ریسورس سفارشی. با این قابلیت، طراح بازی بدون تغییر کد بازیکن می‌تواند به تعداد دلخواه ریسورس و داده تولید کند (نینجا، شوالیه، ربات جنگجو و...).",
                propertiesList = listOf(
                    "@export var character_name: String",
                    "@export var max_health: float = 100",
                    "@export var base_speed: float = 180",
                    "@export var defense: float = 5",
                    "@export var life_steal_pct: float = 0"
                ),
                methodsList = listOf("تعریف یک ریسورس سفارشی با کلمه کلیدی class_name CharacterData"),
                codeContent = """class_name CharacterData
extends Resource

# -------------------------------------------------------------
# CharacterData - منبع داده‌های سفارشی بازیکن (Custom Resource)
# نقش: تعریف مشخصات اولیه بازیکن به صورت داده‌محور (Data-Driven).
# طراحان بازی می‌توانند با ایجاد چند نمونه از این منبع، کلاس‌های متفاوتی
# از قهرمانان بسازند (مثلاً نینجا، شوالیه، جادوگر) بدون دستکاری در کد بازیکن.
# -------------------------------------------------------------

@export var character_name: String = "Survivor"
@export var max_health: float = 100.0
@export var base_speed: float = 180.0
@export var defense: float = 5.0
@export var life_steal_pct: float = 0.0 # درصد جذب جان به همراه آسیب
@export var character_sprite: Texture2D""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://resources/weapon_data.gd",
                fileName = "weapon_data.gd",
                category = "Resource",
                farsiTitle = "منبع داده سفارشی سلاح (WeaponData)",
                farsiDescription = "دیتا کارت انواع سلاح‌های اتوماتیک در بازی‌های سبک بقا. طراح بازی انواع سلاح‌های تیرانداز، پرتابه لیزری و شمشیر چرخنده را با پر کردن فیلدهای این کلاس با مقادیر متفاوت ثبت می‌کند.",
                propertiesList = listOf(
                    "@export var weapon_id: String",
                    "@export var weapon_name: String",
                    "@export var base_damage: float",
                    "@export var projectile_speed: float",
                    "@export var fire_rate: float",
                    "@export var projectile_count: int",
                    "@export var penetration_count: int"
                ),
                methodsList = listOf("ویژگی weapon_scene پیوند به صحنه بصری رندر فیزیکی سلاح"),
                codeContent = """class_name WeaponData
extends Resource

# -------------------------------------------------------------
# WeaponData - منبع داده‌های سفارشی سلاح (Custom Resource)
# نقش: تعریف ساختار داده‌محور برای سلاح‌های خودکار (Auto-Weapons).
# در بازی‌های Survivor، سلاح‌ها پارامترهای تراز و ویژگی‌های خاص خود را دارند.
# با این ریسورس می‌توان جزییات آماری انواع سلاح‌ها را تعریف نمود.
# -------------------------------------------------------------

@export var weapon_id: String = "magic_wand"
@export var weapon_name: String = "Magic Wand"
@export_multiline var description: String = "Fires magical projectiles at the nearest enemy."
@export var base_damage: float = 15.0
@export var projectile_speed: float = 300.0
@export var fire_rate: float = 1.2 # شلیک در ثانیه
@export var projectile_count: int = 1
@export var penetration_count: int = 1
@export var area_of_effect: float = 1.0 # ضریب اندازه شلیک
@export var icon: Texture2D
@export var weapon_scene: PackedScene # لینک به صحنه گرافیکی سلاح""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scenes/player/player.gd",
                fileName = "player.gd",
                category = "Scene",
                farsiTitle = "برنامه و کدهای گره بازیکن (Player)",
                farsiDescription = "هسته فیزیکی و حرکتی بازیکن. دریافت مستقیم جہات آنالوگ از جوی‌استیک مجازی موبایل، ردیابی بلادرنگ تمام دشمنان درون Area2D دورتادور خویش، هدف‌گیری خودکار نزدیک‌ترین زامبی و فعال کردن تایمر شلیک با ویژگی‌های Export و سیگنالینگ جهت توازن بازی.",
                propertiesList = listOf(
                    "@export var attack_speed: float = 1.0",
                    "@export var detection_radius: float = 300.0",
                    "@export var base_speed_override: float = 180.0",
                    "joystick_direction: Vector2",
                    "current_health: float"
                ),
                methodsList = listOf(
                    "signal enemy_targeted(enemy: CharacterBody2D)",
                    "signal player_attacked(target_pos, dmg)",
                    "_physics_process(delta) -> پردازش حرکت آنالوگ مستقل از فریم",
                    "get_nearest_enemy() -> بررسی اجسام Area2D و گزینش نزدیک‌ترین هدف",
                    "_draw() -> رسم پویا و کارای دایره آبی رنگ به عنوان پلیس‌هولدر",
                    "_on_joystick_moved(dir) -> دریافت زاویه پیوسته شست موبایل"
                ),
                codeContent = """class_name Player
extends CharacterBody2D

# -------------------------------------------------------------
# Player - منطق، فیزیک، و کنترل‌کننده بازیکن
# نقش: حرکت دهی فیزیکی به کمک جوی‌استیک آنالوگ مجازی موبایل، ردیابی خودکار
# نزدیک‌ترین دشمنان در محدوده Area2D متصل به کاراکتر، و شلیک خودکار پرتابه‌ها
# با زمان‌بندی دقیق و متغیرهای قابل تنظیم جهت بالانس طراحی بازی.
# -------------------------------------------------------------

# سیگنال‌های سفارشی برای بالانس کردن و اتصال به واسط کاربری
signal enemy_targeted(enemy: CharacterBody2D)
signal player_attacked(target_position: Vector2, damage: float)

# متغیرهای تعریف شده برای تغییر مستقیم و آسان در ادیتور گودو (Export Variables)
@export_group("Stats & Balance")
@export var default_stats: CharacterData
@export var attack_speed: float = 1.0 # فرکانس شلیک پرتابه خودکار (ثانیه بین شلیک‌ها)
@export var detection_radius: float = 300.0 # شعاع جستجوی حلقه Area2D برای دشمنان
@export var base_speed_override: float = 180.0 # سرعت حرکت برای تست و توازن مستقیم

@onready var detection_area: Area2D = #DOLLAR#DetectionArea
@onready var attack_timer: Timer = #DOLLAR#AttackTimer

var stats: CharacterData
var current_health: float = 100.0
var equipped_weapons: Array[WeaponData] = []
var joystick_direction: Vector2 = Vector2.ZERO

func _ready() -> void:
	add_to_group("player")
	
	if default_stats:
		stats = default_stats
	else:
		stats = CharacterData.new()
	
	current_health = stats.max_health
	
	# برقراری ارتباط با کلاس ورودی لمسی (MobileInput) برای دریافت بردار آنالوگ حرکت
	var mobile_inputs = get_tree().get_nodes_in_group("mobile_input")
	for input_node in mobile_inputs:
		if input_node.has_signal("joystick_moved"):
			input_node.connect("joystick_moved", Callable(self, "_on_joystick_moved"))
		if input_node.has_signal("joystick_released"):
			input_node.connect("joystick_released", Callable(self, "_on_joystick_released"))
	
	# پیکربندی اولیه تایمر خودکار شلیک بر اساس متغیر بالانس attack_speed
	if attack_timer:
		attack_timer.wait_time = attack_speed
		attack_timer.one_shot = false
		attack_timer.start()
		attack_timer.connect("timeout", Callable(self, "_on_attack_timer_timeout"))
		
	# تنظیم پویای ابعاد Area2D ردیاب دشمن از روی متغیر بالانس شعاع
	if detection_area:
		var coll_shape = detection_area.get_node_or_null("CollisionShape2D")
		if coll_shape and coll_shape.shape is CircleShape2D:
			coll_shape.shape.radius = detection_radius
			
	EventBus.emit_signal("player_spawned", self)
	EventBus.emit_signal("player_health_changed", current_health, stats.max_health)
	
	# اتصال پاسخگویی به سیگنال ارتقای تراز انتخاب شده
	EventBus.connect("upgrade_selected", Callable(self, "_on_upgrade_selected"))
	
	# فراخوانی رندر مجدد دایره آبی رنگ به عنوان پلیس‌هولدر زیباشناختی
	queue_redraw()

func _draw() -> void:
	# رسم شکل دایره آبی رنگ به عنوان پلیس‌هولدر بصری کاراکتر شوالیه
	draw_circle(Vector2.ZERO, 20.0, Color(0.12, 0.53, 0.9, 1.0)) # دایره اصلی آبی رنگ
	draw_circle(Vector2.ZERO, 23.0, Color(1.0, 1.0, 1.0, 0.45), false, 2.5) # حلقه دورتادور سفید نیمه‌شفاف
	
	# در صورت وجود هدف زنده، خط مایل هدف‌گیری را با ظرافت رسم می‌کند
	var nearest_enemy = get_nearest_enemy()
	if nearest_enemy and is_instance_valid(nearest_enemy):
		var local_target = to_local(nearest_enemy.global_position)
		draw_line(Vector2.ZERO, local_target.limit_length(45.0), Color(0.9, 0.2, 0.2, 0.7), 2.0)

func _physics_process(_delta: float) -> void:
	# دریافت همزمان بردار آنالوگ جوی‌استیک و کیبورد (جهت تست دسکتاپ و موبایل)
	var move_vector = joystick_direction
	if move_vector == Vector2.ZERO:
		move_vector = Input.get_vector("move_left", "move_right", "move_up", "move_down")
	
	# حرکت روان مستقل از فریم فیزیکی
	var current_speed = stats.base_speed if stats else base_speed_override
	velocity = move_vector * current_speed
	move_and_slide()
	
	# به‌روزرسانی پویای گرافیک برای چرخش‌ها یا ترسیم نشانگر هدف‌گیری
	queue_redraw()

func get_nearest_enemy() -> CharacterBody2D:
	if !detection_area:
		return null
		
	var overlapping_bodies = detection_area.get_overlapping_bodies()
	var nearest: CharacterBody2D = null
	var min_dist: float = INF
	
	for body in overlapping_bodies:
		if body is CharacterBody2D and body.is_in_group("enemies") and is_instance_valid(body):
			var dist = global_position.distance_to(body.global_position)
			if dist < min_dist:
				min_dist = dist
				nearest = body
	return nearest

func _on_attack_timer_timeout() -> void:
	var target = get_nearest_enemy()
	if target and is_instance_valid(target):
		emit_signal("enemy_targeted", target)
		shoot_projectile_at(target)

func shoot_projectile_at(enemy: CharacterBody2D) -> void:
	var target_vector = (enemy.global_position - global_position).normalized()
	emit_signal("player_attacked", enemy.global_position, stats.defense if stats else 5.0)
	
	# افکت شلیک به سمت نزدیک‌ترین دشمن
	EventBus.emit_signal("xp_collected", 1, GameManager.current_xp, GameManager.xp_to_next_level) # پاداش نمایشی به ازای برخورد شلیک‌ها

func take_damage(amount: float) -> void:
	var defense_val = stats.defense if stats else 5.0
	var actual_damage = max(amount - defense_val, 1.0)
	current_health -= actual_damage
	EventBus.emit_signal("player_health_changed", current_health, stats.max_health if stats else 100.0)
	
	if current_health <= 0:
		die()

func die() -> void:
	EventBus.emit_signal("player_died")
	queue_free()

func _on_joystick_moved(direction: Vector2) -> void:
	# ورودی آنالوگ جوی‌استیک - بردار جهت لمسی
	joystick_direction = direction

func _on_joystick_released() -> void:
	# رهاشدن انگشت از روی جوی‌استیک مجازی
	joystick_direction = Vector2.ZERO

func _on_upgrade_selected(upgrade_data: UpgradeData) -> void:
	if not upgrade_data:
		return
		
	match upgrade_data.stat_type:
		"damage":
			# اعمال مستقیم دمیج بیشتر به بازیکن
			if stats:
				stats.defense += upgrade_data.stat_value
		"attack_speed":
			# فرکانس کوبش پرتابه‌ها (کاهش میزان تاخیر شلیک)
			attack_speed = max(0.1, attack_speed * (1.0 - upgrade_data.stat_value))
			if attack_timer:
				attack_timer.wait_time = attack_speed
		"speed":
			# بهبود سرعت فیزیکی شوالیه
			if stats:
				stats.base_speed += upgrade_data.stat_value
			else:
				base_speed_override += upgrade_data.stat_value""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scenes/player/player.tscn",
                fileName = "player.tscn",
                category = "Scene",
                farsiTitle = "ساختار بصری درخت صحنه بازیکن (Player.tscn)",
                farsiDescription = "فایل مرجع پیکربندی صحنه گودو ۴ با فرمت متنی استاندارد tscn. تعریف ساختار سلسله‌مراتب گره‌ها شامل گره فیزیکی ریشه (CharacterBody2D)، محدوده شناسایی حلقه Area2D، شکل برخورد بدنه CollisionShape2D و تایمر خودکار شلیک.",
                propertiesList = listOf(
                    "Collision Layer = 2 (Player)",
                    "Collision Mask = 5 (Enemies, Environment)",
                    "DetectionArea (Area2D)",
                    "AttackTimer (Timer)"
                ),
                methodsList = listOf(
                    "اتصال خودکار اسکریپت player.gd",
                    "بارگذاری مشخصات پیش‌فرض بازیکن از روی فایل tres.",
                    "راه‌اندازی تایمر شلیک خودکار به عنوان فرزند زیردرختی"
                ),
                codeContent = """[gd_scene load_steps=5 format=3 uid="uid://c1p8r3b3v2x5p"]

[ext_resource type="Script" path="res://scenes/player/player.gd" id="1_player_gd"]
[ext_resource type="Resource" uid="uid://d3u2r1k4j6m5b" path="res://resources/character_data.tres" id="2_char_data"]

[sub_resource type="CircleShape2D" id="CircleShape2D_p3x2k"]
radius = 20.0

[sub_resource type="CircleShape2D" id="CircleShape2D_a7h3b"]
radius = 300.0

[node name="Player" type="CharacterBody2D" groups=["player"]]
collision_layer = 2
collision_mask = 5
script = ExtResource("1_player_gd")
default_stats = ExtResource("2_char_data")
attack_speed = 1.0
detection_radius = 300.0
base_speed_override = 180.0

[node name="CollisionShape2D" type="CollisionShape2D" parent="."]
shape = SubResource("CircleShape2D_p3x2k")

[node name="DetectionArea" type="Area2D" parent="."]
collision_layer = 0
collision_mask = 4

[node name="CollisionShape2D" type="CollisionShape2D" parent="DetectionArea"]
shape = SubResource("CircleShape2D_a7h3b")
debug_color = Color(0, 0.6, 0.7, 0.42)

[node name="AttackTimer" type="Timer" parent="."]
wait_time = 1.0
autostart = true

[node name="VisualPlaceholder" type="Node2D" parent="."]
# رسم شکل دایره آبی رنگ از طریق متد رسم پویای اسکریپت بازیکن انجام می‌گیرد""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scenes/enemies/enemy_base.gd",
                fileName = "enemy_base.gd",
                category = "Scene",
                farsiTitle = "کد هوش مصنوعی و برخورد دشمن (Enemy)",
                farsiDescription = "هوش مصنوعی و منطق فیزیکی دشمن تفنگ‌به‌دست/زامبی. ردگیری بازیکن در صفحه حرکت فیزیکی، تحمیل صدمه ضربانی دمیج ملموس در هنگام تماس بدنی مستقیم، رسم پویا و منعطف مربع قرمز به عنوان پلیس‌هولدر گرافیکی، و بازگشت به استخر بهینه موبایل (EnemyPool) بجای حذف فیزیکی از رم.",
                propertiesList = listOf(
                    "@export var max_health: float = 30",
                    "@export var speed: float = 120",
                    "@export var damage: float = 10",
                    "xp_value: int, score_value: int",
                    "target_player: CharacterBody2D"
                ),
                methodsList = listOf(
                    "_ready() -> آماده‌سازی جان و ثبت نام در نقشه ترسیم",
                    "_draw() -> ترسیم منعطف و کارای مربع قرمز برحسب ابعاد فیزیکی",
                    "_physics_process(delta) -> تعقیب بازیکن و اعمال صدمه مکرر فیزیکی در صورت برخورد",
                    "take_damage(amount) -> ضربه پذیری از شلیک‌های قهرمان",
                    "die() -> ارسال پاداش مرگ به EventBus و عودت دشمن به استخر"
                ),
                codeContent = """class_name EnemyBase
extends CharacterBody2D

# -------------------------------------------------------------
# EnemyBase - منطق پایه دشمن تعقیب‌گر با استخر اشیاء (Object Pooling)
# نقش: تعقیب بازیکن (Player)، تشخیص فیزیکی برخورد و زدن آسیب (دمیج) مداوم،
# رندر پویا به شکل مربع قرمز (Red Square) و بازگشت خودکار به استخر EnemyPool.
# -------------------------------------------------------------

@export var enemy_name: String = "Zombie"
@export var max_health: float = 30.0
@export var speed: float = 120.0
@export var damage: float = 10.0
@export var xp_value: int = 15
@export var score_value: int = 10

var current_health: float = 30.0
var target_player: CharacterBody2D = null

func _ready() -> void:
	current_health = max_health
	EventBus.connect("player_spawned", Callable(self, "_on_player_spawned"))
	
	# پیدا کردن بازیکن در زمان شروع اگر از قبل اسپون شده باشد
	var players = get_tree().get_nodes_in_group("player")
	if players.size() > 0:
		target_player = players[0]
		
	# فراخوانی توابع رندر شکل هندسی مربع قرمز
	queue_redraw()

func _draw() -> void:
	# رسم مربع قرمز رنگ به عنوان پلیس‌هولدر بصری دشمن مطابق با خواست طراح (Red Square)
	var size = 26.0
	draw_rect(Rect2(-size / 2.0, -size / 2.0, size, size), Color(0.85, 0.15, 0.15, 1.0)) # مربع قرمز اصلی
	draw_rect(Rect2(-size / 2.0, -size / 2.0, size, size), Color(1.0, 1.0, 1.0, 0.5), false, 2.0) # حاشیه سفید نیمه‌شفاف برای افزایش خوانایی

func _physics_process(delta: float) -> void:
	if target_player and is_instance_valid(target_player):
		# حرکت مستقیم به سمت بازیکن (مکانیک اصلی تعقیب در Bullet Heaven)
		var direction = (target_player.global_position - global_position).normalized()
		velocity = direction * speed
		move_and_slide()
		
		# تشخیص برخورد فیزیکی مستقیم با بازیکن و اعمال صدمه (دمیج)
		for i in get_slide_collision_count():
			var collision = get_slide_collision(i)
			var collider = collision.get_collider()
			if collider and collider.is_in_group("player") and collider.has_method("take_damage"):
				collider.take_damage(damage * delta) # آسیب مستمر بر اساس ثانیه زنده ماندن

func take_damage(amount: float) -> void:
	current_health -= amount
	if current_health <= 0:
		die()

func die() -> void:
	# انتشار رویداد مرگ تا GameManager امتیاز را ثبت کند (XP صفر شده چون بلور مجزا در نقشه اسپون می‌شود)
	EventBus.emit_signal("enemy_died", self, score_value, 0)
	
	# ایجاد و اسپاون فیزیکی بلور تجربه (XP Gem) در محل مرگ دشمن
	var gem_scene = load("res://scenes/items/xp_gem.tscn")
	if gem_scene:
		var gem = gem_scene.instantiate()
		gem.global_position = global_position
		gem.xp_value = xp_value
		get_parent().add_child(gem)
	
	# به جای استفاده مخرب از queue_free()، دشمن را غیرفعال کرده و به استخر حافظه باز می‌میگردانیم
	if get_tree().has_group("enemy_pool"):
		var pools = get_tree().get_nodes_in_group("enemy_pool")
		if pools.size() > 0:
			pools[0].return_enemy(self)
			return
			
	# رفتار زاپاس در صورت در دسترس نبودن موقت سیستم استخر
	queue_free()

func _on_player_spawned(player_node: CharacterBody2D) -> void:
	target_player = player_node""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scenes/weapons/weapon_base.gd",
                fileName = "weapon_base.gd",
                category = "Scene",
                farsiTitle = "موتور شلیک خودکار سلاح مجهز (Weapon)",
                farsiDescription = "هسته کنترل‌کننده تمام اسلحه های خودکار بازی‌های بقا. به کمک یک گره تایمر تکرارشونده، هر ثانیه نزدیک‌ترین دشمن نقشه به موقعیت بازیکن را رهگیری می‌کند و تیر را به طرف او گسیل می‌نهد.",
                propertiesList = listOf("@export var weapon_config: WeaponData", "target_enemy: CharacterBody2D"),
                methodsList = listOf(
                    "setup_weapon() -> همگام‌سازی زمان تایمر بر اساس خصیصه fire_rate ریسورس سلاح",
                    "_on_fire_timer_timeout() -> پایان شمارش تایمر و فراخوانی ردیاب دشمن",
                    "get_nearest_enemy() -> بررسی تمام گره‌های دشمن در گروه و یافتن نزدیک‌ترین آن‌ها",
                    "shoot_at_target(enemy) -> اسپون فیزیکی پرتابه به سمت موقعیت هدف"
                ),
                codeContent = """class_name WeaponBase
extends Node2D

# -------------------------------------------------------------
# WeaponBase - منطق پایه شلیک سلاح‌ها
# نقش: فعال‌سازی تایمر شلیک خودکار، یافتن نزدیک‌ترین دشمن در شعاع حمله
# و شلیک پرتابه به سمت هدف با استفاده از اطلاعات WeaponData.
# -------------------------------------------------------------

@export var weapon_config: WeaponData

@onready var fire_timer: Timer = #DOLLAR#FireTimer

var target_enemy: CharacterBody2D = null

func _ready() -> void:
	if weapon_config:
		setup_weapon()

func setup_weapon() -> void:
	# تنظیم طول زمان شلیک بر اساس سرعتی که تراز به سلاح می‌دهد
	fire_timer.wait_time = 1.0 / weapon_config.fire_rate
	fire_timer.start()

func _on_fire_timer_timeout() -> void:
	target_enemy = get_nearest_enemy()
	if target_enemy:
		shoot_at_target(target_enemy)

func get_nearest_enemy() -> CharacterBody2D:
	var enemies = get_tree().get_nodes_in_group("enemies")
	var nearest: CharacterBody2D = null
	var min_dist: float = INF
	
	for enemy in enemies:
		if is_instance_valid(enemy):
			var dist = global_position.distance_to(enemy.global_position)
			if dist < min_dist:
				min_dist = dist
				nearest = enemy
	return nearest

func shoot_at_target(enemy: CharacterBody2D) -> void:
	# اینجا پرتابه (Projectile) از روی صحنهweapon_config.weapon_scene ساخته می‌شود
	# و راستای شلیک به سمت دشمن متغیر می‌گردد.
	pass""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scenes/ui/hud.gd",
                fileName = "hud.gd",
                category = "Scene",
                farsiTitle = "سیستم جامع واسط کاربر (HUD CanvasLayer)",
                farsiDescription = "واسط گرافیکی یکپارچه و بهینه‌سازی شده برای نسبت‌های تصویر موبایل (۱۹:۹ و ۲۰:۹). با بکارگیری لنگرها (Anchors) و پوسته مشترک (ui_theme.tres)، اجزای حیاتی بقا شامل نوار سلامت (بالا)، نوار تجربه پویا (پایین)، کنترل صوتی جوی‌استیک شناور سمت چپ، و پنل انتخاب سه کارتی لول‌آپ را در لایه‌ای ایمن رسم و متصل می‌نماید.",
                propertiesList = listOf(
                    "hp_bar: ProgressBar (بالای صفحه/لنگر بالا-وسط/سرخ)",
                    "xp_bar: ProgressBar (پایین صفحه/لنگر پایین-عریض/آبی فیروزه‌ای)",
                    "timer_label: Label (تایمر صعودی بقا)",
                    "virtual_joystick: Control (جوی‌استیک شناور سمت چپ با تاچ‌پد اختصاصی)",
                    "level_up_panel: Control (پنل کارت‌های سه‌گانه با آیکون‌های placeholder زمان لول‌آپ)"
                ),
                methodsList = listOf(
                    "_ready() -> بارگذاری تم مشترک و ثبات لنگرها در مانیتور موبایل",
                    "_process(delta) -> شمارش منظم ثانیه‌های بقا و همگام‌سازی با گیم منجر",
                    "_on_joystick_dragged(vector) -> انتقال بردار جابجایی تاچ‌پد به پلیر بر اساس لمس مستقیم",
                    "_trigger_level_up_ui() -> توقف فیزیکی کامل گیم‌پلی و فعال‌سازی ۳ کارت ارتقا لمسی",
                    "_on_upgrade_selected(id) -> پاکسازی پانل، بروزرسانی سکه محلی و خروج از Pause"
                ),
                codeContent = """extends CanvasLayer

# -----------------------------------------------------------------------------
# HUD - Heads-Up Display Base System (طراحی منطبق بر نمایش‌های ۱۹:۹ و ۲۰:۹ موبایل)
# مجهز به پوسته سراسری تم ریورس (ui_theme.tres)، لنگرهای منعطف و واکنش‌گرا،
# شبیه‌ساز جوی‌استیک کنترلر شناور و منوی سه‌کارتی پیشرفته گزینش لول‌آپ.
# -----------------------------------------------------------------------------

# پیش‌بارگذاری تم مشترک رنگ و فونت برای یکنواختی کلی بازی
const UI_THEME = preload("res://resources/ui_theme.tres")

# بکارگیری لنگرهای دقیق (Anchors) برای پیشگیری از خروج عناصر در مانیتورهای قدکشیده
@onready var safe_area: Control = #DOLLAR#SafeArea
@onready var hp_bar: ProgressBar = #DOLLAR#SafeArea/TopHUD/HPBar
@onready var timer_label: Label = #DOLLAR#SafeArea/TopHUD/TimerLabel
@onready var xp_bar: ProgressBar = #DOLLAR#SafeArea/BottomHUD/XPBar
@onready var lvl_label: Label = #DOLLAR#SafeArea/BottomHUD/LevelLabel

# جوی‌استیک شناور واقع در سمت چپ پایین صفحه
@onready var joystick_container: Control = #DOLLAR#SafeArea/LeftHUD/VirtualJoystick
@onready var joystick_knob: TextureRect = #DOLLAR#SafeArea/LeftHUD/VirtualJoystick/Knob

# پنل مستقل انتخاب ارتقاهای لول آپ (Level-Up Cards Modal)
@onready var level_up_panel: Panel = #DOLLAR#SafeArea/LevelUpPanel
@onready var card_1_btn: Button = #DOLLAR#SafeArea/LevelUpPanel/Card1
@onready var card_2_btn: Button = #DOLLAR#SafeArea/LevelUpPanel/Card2
@onready var card_3_btn: Button = #DOLLAR#SafeArea/LevelUpPanel/Card3

var joystick_active: bool = false
var joystick_start_pos: Vector2 = Vector2.ZERO
var max_joystick_drag: float = 65.0 # حداکثر شعاع جابجایی دایره داخلی

func _ready() -> void:
	# اعمال تم مشترک به صورت پویا به کل لایه HUD
	self.theme = UI_THEME
	
	# پنهان بودن پنل لول‌آپ در شروع بازی
	if level_up_panel:
		level_up_panel.visible = false
		
	# تنظیم واکنش‌گرای لنگرها (Set Anchors for 19:9 / 20:9 Compatibility)
	_setup_responsive_anchors()

	# اتصال به سیگنال‌های اتوبوس رویدادها (EventBus)
	EventBus.connect("player_health_changed", Callable(self, "_on_player_health_changed"))
	EventBus.connect("xp_collected", Callable(self, "_on_xp_collected"))
	EventBus.connect("player_level_up", Callable(self, "_on_player_level_up"))
	
	# اتصال کلیک‌های کارت‌های لول‌آپ با متد ارتقا
	if card_1_btn:
		card_1_btn.pressed.connect(func(): _on_upgrade_selected("damage_boost"))
	if card_2_btn:
		card_2_btn.pressed.connect(func(): _on_upgrade_selected("attack_rate_boost"))
	if card_3_btn:
		card_3_btn.pressed.connect(func(): _on_upgrade_selected("speed_boost"))

func _process(_delta: float) -> void:
	if GameManager.is_game_active:
		timer_label.text = format_time(GameManager.time_elapsed)

func format_time(time_in_seconds: float) -> String:
	var minutes = int(time_in_seconds) / 60
	var seconds = int(time_in_seconds) % 60
	return "%02d:%02d" % [minutes, seconds]

# -------------------------------------------------------------
# تنظیمات واکنش‌گرای لنگرها چیدمان بومی مانیتور
# -------------------------------------------------------------
func _setup_responsive_anchors() -> void:
	# Safe area لنگراندازی بالا و پایین صفحه را برای ابعاد موبایل تضمین می‌کند
	if safe_area:
		safe_area.anchor_left = 0.0
		safe_area.anchor_right = 1.0
		safe_area.anchor_top = 0.0
		safe_area.anchor_bottom = 1.0
		# ایجاد حاشیه مطمئن لمسی جهت سازگاری با بریدگی ناچ بالای صفحه نمایش‌های موبایل
		safe_area.offset_top = 40.0 
		safe_area.offset_bottom = -24.0

# -------------------------------------------------------------
# پردازش لمسی و محاسباتی جوی‌استیک شناور لمسی چپ
# -------------------------------------------------------------
func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed:
			# اگر لمس در ربع پایین و چپ تصویر اتفاق افتاد، جوی‌استیک را شناور و نمایان ساز
			if event.position.x < get_viewport().size.x / 2 and event.position.y > get_viewport().size.y * 0.5:
				joystick_active = true
				joystick_start_pos = event.position
				joystick_container.global_position = event.position - (joystick_container.size / 2.0)
				joystick_knob.position = (joystick_container.size / 2.0) - (joystick_knob.size / 2.0)
		else:
			if joystick_active:
				joystick_active = false
				joystick_knob.position = (joystick_container.size / 2.0) - (joystick_knob.size / 2.0)
				EventBus.emit_signal("joystick_vector_changed", Vector2.ZERO)
				
	elif event is InputEventScreenDrag and joystick_active:
		# محاسبه تفاضل بردار حرکت و جهت‌دهی به بازیکن
		var drag_offset = event.position - joystick_start_pos
		if drag_offset.length() > max_joystick_drag:
			drag_offset = drag_offset.normalized() * max_joystick_drag
			
		joystick_knob.position = (joystick_container.size / 2.0) - (joystick_knob.size / 2.0) + drag_offset
		var norm_direction = drag_offset / max_joystick_drag
		EventBus.emit_signal("joystick_vector_changed", norm_direction)

# -------------------------------------------------------------
# اتصالات سیگنالی و بازخورد آماری
# -------------------------------------------------------------
func _on_player_health_changed(current: float, max_val: float) -> void:
	if hp_bar:
		hp_bar.max_value = max_val
		hp_bar.value = current

func _on_xp_collected(_amt: int, current: int, next_lvl_xp: int) -> void:
	if xp_bar:
		xp_bar.max_value = next_lvl_xp
		xp_bar.value = current

func _on_player_level_up(new_level: int) -> void:
	if lvl_label:
		lvl_label.text = "LVL: " + str(new_level)
	
	# توقف شبیه‌سازی فیزیک بازی برای تعامل با کارت‌های ارتقاء لول‌آپ
	_trigger_level_up_ui()

func _trigger_level_up_ui() -> void:
	get_tree().paused = true
	if level_up_panel:
		level_up_panel.visible = true
		
		# تنظیم متون و آیکون‌های نمونه به صورت تصادفی جهت لمس روان
		_populate_placeholder_cards()

func _populate_placeholder_cards() -> void:
	# تعیین محتویات کارت با آیکون‌های placeholder ملموس برای سهولت کاربری
	if card_1_btn:
		card_1_btn.text = "⚔️ [Card 1] Heavy Strike\nDamage +15% (Melee)"
	if card_2_btn:
		card_2_btn.text = "⚡ [Card 2] Wind Boots\nMovement Speed +10%"
	if card_3_btn:
		card_3_btn.text = "🛡️ [Card 3] Guardian Armor\nBase Defense +5"

func _on_upgrade_selected(upgrade_id: String) -> void:
	# خروج از توقف فیزیکی و آغاز مجدد نبرد شوالیه جوان
	get_tree().paused = false
	if level_up_panel:
		level_up_panel.visible = false
	
	# اعلام اعمال ارتقاء انتخابی
	EventBus.emit_signal("upgrade_applied", upgrade_id)
	
	# افزودن هدیه طلای تشویقی به متغیرهای SaveManager محلی
	SaveManager.add_coins(10)""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scenes/levels/main_level.gd",
                fileName = "main_level.gd",
                category = "Scene",
                farsiTitle = "صحنه اصلی جهان و سیستم احضار (Main Level)",
                farsiDescription = "اتاق کلی نقشه بازی بقا. ساخت پس زمینه بازی، به کار انداختن GameManager به محض لود، و تعیین مختصات تصادفی دایره‌ای روی دوربین به فواصل معینی دور تر از بازیکن برای اسپون دشمنان.",
                propertiesList = listOf("@export var enemy_scene: PackedScene", "spawn_timer: Timer"),
                methodsList = listOf(
                    "_ready() -> اعلام آغاز رسمی بازی به گیم منیجر و آماده‌سازی تایمر اسپونر",
                    "_on_spawn_timer_timeout() -> فراخوانی اسپونر دشمن دور تا دور موقعیت بازیکن",
                    "spawn_enemy_near_player() -> ایجاد دشمن جدید و رفرنس دادن موقعیت مکانی خارج از دید دوربین"
                ),
                codeContent = """extends Node2D

# -------------------------------------------------------------
# MainLevel - مدیریت صحنه اصلی گیم‌پلی و اسپون دشمنان
# نقش: لود کردن صحنه هاب گرافیکی بازیکن، راه‌اندازی تایمر تولید
# گروهی دشمنان دور تا دور صفحه دوربین، و فعال‌سازی بازی از طریق GameManager.
# -------------------------------------------------------------

@export var enemy_scene: PackedScene = preload("res://scenes/enemies/enemy_base.scn") if ResourceLoader.exists("res://scenes/enemies/enemy_base.scn") else null

@onready var spawn_timer: Timer = #DOLLAR#SpawnTimer
@onready var player: Player = #DOLLAR#Player

func _ready() -> void:
	# شروع اتوماتیک بازی به طور کاملاً آفلاین
	GameManager.start_game()
	spawn_timer.start()

func _on_spawn_timer_timeout() -> void:
	if GameManager.is_game_active and player:
		spawn_enemy_near_player()

func spawn_enemy_near_player() -> void:
	if !enemy_scene:
		return
	var enemy_instance = enemy_scene.instantiate() as CharacterBody2D
	
	# ایجاد موقعیت تصادفی دایره‌ای دور بازیکن خارج از افق دید دوربین
	var spawn_radius = 500.0
	var angle = randf() * TAU
	var spawn_pos = player.global_position + Vector2(cos(angle), sin(angle)) * spawn_radius
	
	enemy_instance.global_position = spawn_pos
	add_child(enemy_instance)""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scripts/mobile_input.gd",
                fileName = "mobile_input.gd",
                category = "Script",
                farsiTitle = "مدیریت انگشت روی صفحه موبایل (MobileInput)",
                farsiDescription = "اسکریپت و منطق کمکی سیستم تاچ موبایل. تبدیل کشیده شدن ناگهانی انگشت (Swipe / Drag) به بردارهای جهتی نرمالایز شده جهت حرکت روان بازیکن با یک شست روی موبایل بدون فوت وقت.",
                propertiesList = listOf("touch_start_position: Vector2", "is_dragging: bool", "drag_threshold: float = 10.0"),
                methodsList = listOf(
                    "joystick_moved(direction) -> فرستادن زاویه بردار حرکت",
                    "joystick_released -> رها شدن انگشت از روی مانیتور لمسی دستگاه",
                    "_unhandled_input(event) -> پردازش مداوم رفتارهای لمسی به طور سراسری"
                ),
                codeContent = """class_name MobileInput
extends Node

# -------------------------------------------------------------
# MobileInput - تحلیل ورودی لمسی روی موبایل
# نقش: پیاده‌سازی جوی‌استیک لمسی مجازی یا ژست‌های سوایپ
# برای جابجایی راحت قهرمان بازی با یک انگشت (Single-finger controller).
# -------------------------------------------------------------

var touch_start_position: Vector2 = Vector2.ZERO
var is_dragging: bool = false
var drag_threshold: float = 10.0

signal joystick_moved(direction: Vector2)
signal joystick_released

func _unhandled_input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed:
			touch_start_position = event.position
			is_dragging = true
		else:
			is_dragging = false
			emit_signal("joystick_released")
			
	elif event is InputEventScreenDrag and is_dragging:
		var current_pos = event.position
		var delta = current_pos - touch_start_position
		
		if delta.length() > drag_threshold:
			var movement_direction = delta.normalized()
			emit_signal("joystick_moved", movement_direction)""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://autoload/enemy_pool.gd",
                fileName = "enemy_pool.gd",
                category = "Autoload",
                farsiTitle = "استخر اشیاء بهینه دشمنان (EnemyPool)",
                farsiDescription = "سیستم پیشرفته استخر اشیاء دشمن برای پلتفرم موبایل. پیش‌ساختن تعداد اولیه ۵۰ زامبی جهت کاهش چشمگیر لک ناشی از لود مداوم رم و پاکسازی زباله (GC). تدارک متدهای گرفتن دشمن فعال و عودت به لیست غیرفعال.",
                propertiesList = listOf(
                    "pool_size: int = 50",
                    "enemy_scene: PackedScene",
                    "_pool: Array[CharacterBody2D]"
                ),
                methodsList = listOf(
                    "_ready() -> تولید زودهنگام اشیاء و مدیریت بارگذاری حافظه",
                    "_create_new_pool_element() -> ساخت عضو جدید در استخر",
                    "get_enemy() -> دریافت یک ارجاع غیرفعال آماده",
                    "return_enemy(enemy) -> ریست فیزیکی جهت رجعت به استخر"
                ),
                codeContent = """extends Node

# -------------------------------------------------------------
# EnemyPool - سیستم بهینه استخر اشیاء دشمنان (Object Pooling Pattern)
# نقش: بهینه‌سازی بار پردازشی موبایل با ممانعت فعال از instantiate و queue_free مکرر.
# دشمنان غیرفعال در شروع بازی تخصیص داده شده و حین گیم‌پلی فقط فعال/غیرفعال می‌شوند.
# -------------------------------------------------------------

@export var pool_size: int = 50
@export var enemy_scene: PackedScene = preload("res://scenes/enemies/enemy_base.scn")

var _pool: Array[CharacterBody2D] = []

func _ready() -> void:
	add_to_group("enemy_pool")
	# تولید زودهنگام به محض لود بازی جهت ممانعت از کرش و لک لمسی
	for i in range(pool_size):
		_create_new_pool_element()

func _create_new_pool_element() -> CharacterBody2D:
	if not enemy_scene:
		enemy_scene = load("res://scenes/enemies/enemy_base.scn")
		if not enemy_scene:
			return null
			
	var enemy = enemy_scene.instantiate() as CharacterBody2D
	enemy.visible = false
	enemy.process_mode = Node.PROCESS_MODE_DISABLED
	
	# الصاق به درخت صحنه به عنوان زیرمجموعه غیرفعال این مینی‌لودر
	add_child(enemy)
	_pool.append(enemy)
	return enemy

# درخواست دشمن آماده به کار جهت زنده شدن در امواج
func get_enemy() -> CharacterBody2D:
	for enemy in _pool:
		if is_instance_valid(enemy) and enemy.process_mode == Node.PROCESS_MODE_DISABLED:
			return enemy
	# گسترش پویا و امن در صورت فراتر رفتن تعداد لشکر دشمنان نسبت به ظرفیت استخر
	return _create_new_pool_element()

# عودت دشمن پس از بمباران یا برخورد، به عنوان آیتم غیرفعال دوباره در استخر
func return_enemy(enemy: CharacterBody2D) -> void:
	if is_instance_valid(enemy):
		enemy.visible = false
		enemy.process_mode = Node.PROCESS_MODE_DISABLED
		enemy.velocity = Vector2.ZERO""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scenes/levels/enemy_spawner.gd",
                fileName = "enemy_spawner.gd",
                category = "Script",
                farsiTitle = "اسپاونر امواج با ریسورس سفارشی (EnemySpawner)",
                farsiDescription = "کنترل‌کننده مرکزی تولید دشمنان. هر X ثانیه (برحسب خصوصیت spawn_interval ریسورس فعال موج)، یک موج دشمن از استخر بهینه دریافت نمونه کرده و در فاصله‌ای خارج از دید زاویه دوربین موبایل احضار می‌کند.",
                propertiesList = listOf(
                    "wave_config: WaveData",
                    "spawn_radius: float = 750.0",
                    "spawn_timer: Timer"
                ),
                methodsList = listOf(
                    "_ready() -> هماهنگ‌سازی اتصالات با اتوبوس رویدادها",
                    "apply_wave_configuration() -> پیکربندی مدت تایمر بر اساس اطلاعات ریسورس موج جاری",
                    "_on_spawn_timeout() -> زمان‌سنجی برای تحریک امواج یا تک‌اسپاون‌ها",
                    "spawn_enemy_wave() -> فعال‌سازی دشمن از استخر و ست کردن مکان به صورت خارج از دید"
                ),
                codeContent = """extends Node2D

# -------------------------------------------------------------
# EnemySpawner - اسپاونر امواج هوشمند با پیکربندی ریسورس بیرونی
# نقش: ریتم‌دهی به امواج و فرکانس احضار دشمنان با خواندن مستقیم WaveData.
# قرارگذاری فیزیکی در یک دایره تصادفی ۳۶۰ درجه به فواصل فراتر از دید بازیکن.
# -------------------------------------------------------------

@export var wave_config: WaveData # ارجاع به ریسورس طراحی امواج بدون هاردکد کردن مقادیر
@export var spawn_radius: float = 750.0 # خارج از کادر دوربین معمولی گوشی

@onready var spawn_timer: Timer = #DOLLAR#SpawnTimer

var target_player: CharacterBody2D = null

func _ready() -> void:
	EventBus.connect("player_spawned", Callable(self, "_on_player_spawned"))
	
	# یافتن فوری بازیکن
	var players = get_tree().get_nodes_in_group("player")
	if players.size() > 0:
		target_player = players[0]
		
	if spawn_timer:
		spawn_timer.connect("timeout", Callable(self, "_on_spawn_timeout"))
		apply_wave_configuration()

func apply_wave_configuration() -> void:
	if !wave_config or !spawn_timer:
		return
		
	# تنظیم دینامیک زمان شلیک و تکثیر دشمنان بر اساس مقدار ریسورس
	spawn_timer.wait_time = wave_config.spawn_interval
	spawn_timer.start()
	EventBus.emit_signal("wave_started", wave_config.wave_index)

func _on_spawn_timeout() -> void:
	if GameManager.is_game_active and target_player:
		spawn_enemy_wave()

func spawn_enemy_wave() -> void:
	var enemy = EnemyPool.get_enemy()
	if !enemy:
		return
		
	# تزریق مستقیم آمارهای موج (دمیج ضربه، جان و سرعت تعقیب) از روی ریسورس مانیتور شده موج فعال
	if wave_config:
		enemy.damage = wave_config.enemy_damage
		enemy.speed = wave_config.enemy_speed
		enemy.max_health = wave_config.enemy_max_health
		enemy.current_health = wave_config.enemy_max_health
		
	# تعیین موقعیت تصادفی دایره‌ای ۳۶۰ درجه دور بازیکن خارج از افق دید دوربین
	var angle = randf() * TAU
	var spawn_pos = target_player.global_position + Vector2(cos(angle), sin(angle)) * spawn_radius
	
	enemy.global_position = spawn_pos
	
	# بیدارباش بصری و فیزیکی بدون فرآیند سنگین ایجاد مجدد شیء در حافظه
	enemy.visible = true
	enemy.process_mode = Node.PROCESS_MODE_INHERIT
	
	# ست کردن ارجاع هدف
	enemy.target_player = target_player
	
	# بیدار کردن مجدد گرافیک محلی دشمن (مربع قرمز)
	enemy.queue_redraw()

func _on_player_spawned(player_node: CharacterBody2D) -> void:
	target_player = player_node""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://resources/wave_data.gd",
                fileName = "wave_data.gd",
                category = "Resource",
                farsiTitle = "منبع داده سفارشی موج دشمن (WaveData)",
                farsiDescription = "تعریف ساختار داده‌ای هر موج بقاء به کلاسی با ویژگی Export در گودو ۴. طراح بازی بدون نیاز به ویرایش کدها، می‌تواند فایل‌های متعددی نظیر Wave1.tres یا WaveBoss.tres تنظیم و در اسپاونر آپلود نماید.",
                propertiesList = listOf(
                    "@export var wave_index: int = 1",
                    "@export var spawn_interval: float = 2.5",
                    "@export var enemy_damage: float = 12.0",
                    "@export var enemy_speed: float = 110.0",
                    "@export var enemy_max_health: float = 40.0"
                ),
                methodsList = listOf(
                    "معرفی ریسورس شخصی‌سازی شده با کادر کلاس class_name WaveData"
                ),
                codeContent = """class_name WaveData
extends Resource

# -------------------------------------------------------------
# WaveData - منبع داده‌های سفارشی موج‌های دشمن (Custom Resource)
# نقش: کاتالوگ پلیمورفیک مشخصات گیم‌پلی موج‌ها؛ دمیج دشمنان، سرعت تکثیر (Spawn Interval)
# و آمار فیزیکی جهت توازن همزمان در پنل طراحی، بدون هاردکد.
# -------------------------------------------------------------

@export_group("Wave Mechanics")
@export var wave_index: int = 1
@export var spawn_interval: float = 2.0 # زمان فاصله بین احضار دشمنان به ثانیه

@export_group("Spawned Enemy Stats")
@export var enemy_damage: float = 12.0 # دمیج برخورد مربع‌های قرمز
@export var enemy_speed: float = 110.0 # سرعت حرکت به سمت شوالیه
@export var enemy_max_health: float = 40.0 # جان کلی""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://resources/upgrade_data.gd",
                fileName = "upgrade_data.gd",
                category = "Resource",
                farsiTitle = "کارت سفارشی داده ارتقاء (UpgradeData)",
                farsiDescription = "تعریف ساختار کارت‌های ارتقای لول‌آپ. هر ریسورس از نوع UpgradeData دارای مشخصه نوع صفت هدف (دمیج ضربه، سرعت حمله، یا سرعت قهرمان) و ضریب یا مقدار اعمال افزایش به بازیکن است.",
                propertiesList = listOf(
                    "@export var upgrade_id: String",
                    "@export var upgrade_name: String",
                    "@export var description: String",
                    "@export var stat_type: String # 'damage' or 'attack_speed' or 'speed'",
                    "@export var stat_value: float"
                ),
                methodsList = listOf("کلاس حامل داده برای اتصال به پنل لول‌آپ"),
                codeContent = """class_name UpgradeData
extends Resource

# -------------------------------------------------------------
# UpgradeData - منبع داده سفارشی کارت‌های ارتقای تراز (Custom Resource)
# نقش: تعریف داده‌محور کارت‌های قرعه‌کشی سیستم لول‌آپ.
# گزینه‌هایی نظیر: بهبود دمیج ضربه، فرکانس کوبش پرتابه یا سرعت فیزیکی.
# -------------------------------------------------------------

@export_group("Visual ID")
@export var upgrade_id: String = "damage_up"
@export var upgrade_name: String = "Heavy Strike"
@export_multiline var description: String = "Increases attack damage by +20%."
@export var icon: Texture2D

@export_group("Effect Action")
# انواع صفات قابل تغییر: 'damage', 'attack_speed', 'speed'
@export var stat_type: String = "damage"
@export var stat_value: float = 0.20 # درصد یا مقدار عددی مستقیم ارتقا""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://resources/ui_theme.tres",
                fileName = "ui_theme.tres",
                category = "Resource",
                farsiTitle = "منبع تم اشتراکی واسط کاربر (UITheme)",
                farsiDescription = "منبع تنظیمات یکپارچه استایل، فونت، ابعاد، و رنگ‌های دکمه‌ها و پروگرسبارهای بازی. با اتصال این منبع تم به گره اصلی HUD، رنگ‌بندی تمامی نماهای مربوط به جان مبارز، لول و دکمه‌های بافت هماهنگ باقی می‌ماند.",
                propertiesList = listOf(
                    "Primary Color = #88C0D0 (Ice Blue)",
                    "Secondary Color = #81A1C1",
                    "Danger Accent = #BF616A (Nord Red)",
                    "Success Accent = #A3BE8C (Olive Green)",
                    "Background Base = #2E3440"
                ),
                methodsList = listOf(
                    "اعمال فونت پیش‌فرض برای متون انگلیسی/فارسی",
                    "تعریف StyleBoxFlat برای نوار سلامتی (سرخ رنگ) و نوار پیشرفت تجربی (آبی رنگ)"
                ),
                codeContent = """[gd_resource type="Theme" load_steps=6 format=3]

[sub_resource type="StyleBoxFlat" id="StyleBoxFlat_hp"]
bg_color = Color(0.749, 0.380, 0.416, 1.0) # سرخ اسکاندیناوی
border_width_left = 2
border_width_top = 2
border_width_right = 2
border_width_bottom = 2
border_color = Color(0.18, 0.20, 0.25, 0.8)
corner_radius_top_left = 8
corner_radius_top_right = 8
corner_radius_bottom_right = 8
corner_radius_bottom_left = 8

[sub_resource type="StyleBoxFlat" id="StyleBoxFlat_xp"]
bg_color = Color(0.533, 0.753, 0.816, 1.0) # آبی فیروزه‌ای
border_width_left = 2
border_width_top = 2
border_width_right = 2
border_width_bottom = 2
border_color = Color(0.18, 0.20, 0.25, 0.8)
corner_radius_top_left = 4
corner_radius_top_right = 4
corner_radius_bottom_right = 4
corner_radius_bottom_left = 4

[sub_resource type="StyleBoxFlat" id="StyleBoxFlat_panel_bg"]
bg_color = Color(0.180, 0.204, 0.251, 0.95) # خاکستری مات
border_width_left = 3
border_width_top = 3
border_width_right = 3
border_width_bottom = 3
border_color = Color(0.533, 0.753, 0.816, 0.5)
corner_radius_top_left = 16
corner_radius_top_right = 16
corner_radius_bottom_right = 16
corner_radius_bottom_left = 16

[resource]
ProgressBar/styles/hp_fill = SubResource("StyleBoxFlat_hp")
ProgressBar/styles/xp_fill = SubResource("StyleBoxFlat_xp")
Panel/styles/panel = SubResource("StyleBoxFlat_panel_bg")
Button/colors/font_color = Color(1, 1, 1, 1)
Button/font_sizes/font_size = 14
""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scenes/items/xp_gem.gd",
                fileName = "xp_gem.gd",
                category = "Scene",
                farsiTitle = "بلور جذب شونده تجربه (XPGem)",
                farsiDescription = "اسکریپت بلورهای ردیابی تجربه. پس از مرگ دشمن، یکی از این بلورها در صحنه ظاهر می‌شود. اگر بازیکن در شعاع مغناطیسی مناسب قرار گیرد، بلور با حرکتی شتاب‌دار و دورانی به سمت بازیکن پرواز کرده و در زمان برخورد ناپدید شده و به تجربه او می‌افزاید.",
                propertiesList = listOf(
                    "xp_value: int = 15",
                    "collection_range: float = 160.0",
                    "move_speed: float = 50.0",
                    "acceleration: float = 400.0",
                    "target_player: Player = null"
                ),
                methodsList = listOf(
                    "_physics_process(delta) -> رصد فاصله تا بازیکن و مکش مغناطیسی پر سرعت",
                    "_draw() -> رسم گرافیک درخشنده الماس فیروزه‌ای",
                    "collect() -> تزریق تجربه به GameManager و عودت آزادانه به حافظه"
                ),
                codeContent = """class_name XPGem
extends Area2D

# -------------------------------------------------------------
# XPGem - بلور تجربه با ویژگی مکش مغناطیسی (XP Magnet behavior)
# نقش: ایجاد یک پاداش فیزیکی در محل نابودی دشمنان. با ورود بازیکن به شعاع
# تعیین شده، بلور فعال شده و با شتاب پیوسته به سمت بازیکن روانه و جذب می‌شود.
# -------------------------------------------------------------

@export var xp_value: int = 15
@export var collection_range: float = 160.0 # شعاع عملکرد آهنربای تجربه
@export var max_speed: float = 600.0

var move_speed: float = 80.0
var acceleration: float = 500.0
var target_player: CharacterBody2D = null
var _is_collecting: bool = false

func _ready() -> void:
	add_to_group("xp_gems")
	# پیکربندی لایه‌های برخورد برای بهینه‌سازی
	collision_layer = 8 # لایه جوایز
	collision_mask = 2 # لایه بازیکن

func _draw() -> void:
	# رسم الماس فیروزه‌ای رنگ به صورت پویا با هاله درخشنده
	var points = PackedVector2Array([
		Vector2(0, -9),
		Vector2(7, 0),
		Vector2(0, 9),
		Vector2(-7, 0)
	])
	# هاله بیرونی الماس
	draw_polyline(points, Color(0.15, 0.85, 0.9, 0.5), 3.0, true)
	# پرکردن بدنه الماس
	draw_colored_polygon(points, Color(0.3, 0.95, 1.0, 0.9))

func _physics_process(delta: float) -> void:
	if not GameManager.is_game_active:
		return
		
	# تلاش برای شناسایی بازیکن اگر از قبل ردیابی نشده باشد
	if not target_player:
		var players = get_tree().get_nodes_in_group("player")
		if players.size() > 0:
			var player_node = players[0]
			var dist = global_position.distance_to(player_node.global_position)
			if dist <= collection_range:
				target_player = player_node
	
	# اگر آهنربا فعال شده است، بلور را با شتاب به سمت بازیکن حرکت بده
	if target_player and is_instance_valid(target_player):
		_is_collecting = true
		var direction = (target_player.global_position - global_position).normalized()
		move_speed = min(move_speed + acceleration * delta, max_speed)
		global_position += direction * move_speed * delta
		
		# بررسی برخورد فیزیکی و جذب قطعی بلور
		var dist_to_player = global_position.distance_to(target_player.global_position)
		if dist_to_player < 18.0:
			collect()

func collect() -> void:
	# ارسال صدای پاداش یا افکت بصری ذرات فیروزه‌ای
	GameManager.add_xp(xp_value)
	queue_free()""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scenes/ui/upgrade_panel.gd",
                fileName = "upgrade_panel.gd",
                category = "Scene",
                farsiTitle = "پنل متحرک گزینش ارتقای تراز (UpgradePanel)",
                farsiDescription = "کنترل‌کننده منوی توقف در حالت لول‌آپ. به محض ارتقاء لول بازیکن، این پنل فعال شده، بازی را متوقف کرده، ۳ کارت ارتقا تصادفی را نمایش داده و پس از انتخاب کاربر، سیگنال اعمال را صادر کرده و بازی را از حالت توقف خارج می‌کند.",
                propertiesList = listOf(
                    "all_upgrades: Array[UpgradeData] = []",
                    "active_options: Array[UpgradeData] = []",
                    "panel_ui: Control"
                ),
                methodsList = listOf(
                    "_ready() -> گوش دادن به سیگنال لول آپ EventBus",
                    "show_options() -> توقف روند فیزیکی بازی و قرعه‌کشی تصادفی ۳ کارت",
                    "select_option(index) -> اعمال تغییرات از طریق سیگنال و آزاد کردن توقف"
                ),
                codeContent = """extends CanvasLayer

# -------------------------------------------------------------
# UpgradePanel - سیستم تعاملی ارتقا تراز با منطق بازداری (Pause Loop)
# نقش: توقف کامل گیم‌پلی بازی، انتخاب تصادفی ۳ کارت ارتقا فیزیکی
# از کاتالوگ منابع UpgradeData، و انتشار سیگنال ارتقا پس از لمس کلیدها.
# -------------------------------------------------------------

@export var all_upgrades: Array[UpgradeData] = []

@onready var container_node: Control = #DOLLAR#Control
@onready var option_buttons: Array[Button] = [
	#DOLLAR#Control/Option1,
	#DOLLAR#Control/Option2,
	#DOLLAR#Control/Option3
]

var active_choices: Array[UpgradeData] = []

func _ready() -> void:
	# مخفی نگه داشتن اولیه پنل
	if container_node:
		container_node.visible = false
		
	# ثبت نام در رویداد تراز یابی سراسری بازی
	EventBus.connect("player_level_up", Callable(self, "_on_player_level_up"))

func _on_player_level_up(_new_level: int) -> void:
	# متوقف کردن فیزیکی بازی به محض رخ دادن بالارفتن تراز
	get_tree().paused = true
	
	# فعال کردن بصری منو
	if container_node:
		container_node.visible = true
	
	# قرعه کشی تصادفی سه کارت منحصر به فرد ارتقا
	_draw_random_upgrades()

func _draw_random_upgrades() -> void:
	active_choices.clear()
	
	# کپی موقت از لیست کل ارتقاها برای تضمین قرعه‌کشی بدون تکرار
	var temp_list = all_upgrades.duplicate()
	temp_list.shuffle()
	
	# انتخاب حداکثر ۳ ارتقای متفاوت
	for i in range(min(3, temp_list.size())):
		active_choices.append(temp_list[i])
		
	# پرکردن دکمه‌ها و کادرهای گرافیکی انتخاب روی موبایل
	for i in range(option_buttons.size()):
		if i < active_choices.size():
			var upgrade = active_choices[i]
			option_buttons[i].visible = true
			option_buttons[i].text = upgrade.upgrade_name + "\n" + upgrade.description
		else:
			option_buttons[i].visible = false

# متد متصل به رویداد کلیک هر دکمه (مثلاً کل دکمه شماره ۱)
func _on_option_selected(index: int) -> void:
	if index >= active_choices.size():
		return
		
	var chosen_upgrade = active_choices[index]
	
	# انتشار سیگنال ارتقای انتخاب شده در اتوبوس رویدادها
	EventBus.emit_signal("upgrade_selected", chosen_upgrade)
	
	# بستن منو و از سرگیری جریان بازی هیجانی
	if container_node:
		container_node.visible = false
	get_tree().paused = false""".replace("#DOLLAR#", "$")
            ),
            GodotFileItem(
                virtualPath = "res://scenes/ui/game_over.gd",
                fileName = "game_over.gd",
                category = "Scene",
                farsiTitle = "کنترل‌کننده صفحه باخت بازی (GameOver)",
                farsiDescription = "کنترل‌کننده صفحه خلاصه باخت بازی. با شنیدن سیگنال باخت از اتوبوس رویدادها فعال شده، فیزیک بازی را متوقف کرده، زمان نهایی بقا و تجربه کل را نمایش داده و آمارها را در SaveManager ثبت و با دکمه Restart بازی را از تراز صفر ریبوت می‌کند.",
                propertiesList = listOf(
                    "survival_time_label: Label",
                    "final_xp_label: Label",
                    "best_time_label: Label",
                    "restart_button: Button"
                ),
                methodsList = listOf(
                    "_ready() -> ثبت نام در سیگنال game_over و مخفی‌سازی بومی",
                    "_on_game_over(time_survived, final_xp) -> نمایش آمار بقا، تجربه و رکورد بومی",
                    "_on_restart_pressed() -> شروع مجدد گیم‌پلی و بارگذاری مجدد صحنه"
                ),
                codeContent = """extends CanvasLayer

# -------------------------------------------------------------
# GameOver - اسکریپت رصد باخت شوالیه و ثبت همزمان رکوردهای بومی
# نقش: همگام‌سازی زمان زنده ماندن و تجربه با SaveManager و به روز رسانی رکوردها
# -------------------------------------------------------------

@onready var container_node: Control = #DOLLAR#Control
@onready var survival_time_label: Label = #DOLLAR#Control/SurvivalTimeLabel
@onready var final_xp_label: Label = #DOLLAR#Control/FinalXPLabel
@onready var best_time_label: Label = #DOLLAR#Control/BestTimeLabel

func _ready() -> void:
	if container_node:
		container_node.visible = false
	
	# گوش فرا دادن به رویداد باخت سراسری
	EventBus.connect("game_over", Callable(self, "_on_game_over"))

func _on_game_over(time_survived: float, final_xp: int) -> void:
	# متوقف کردن فیزیکی بازی برای انتخاب مجدد صلح‌آمیز
	get_tree().paused = true
	
	if container_node:
		container_node.visible = true
	
	# تبدیل ثانیه‌ها به فرمت خوانای دقیقه:ثانیه
	var minutes = int(time_survived) / 60
	var seconds = int(time_survived) % 60
	var time_string = "%02d:%02d" % [minutes, seconds]
	
	if survival_time_label:
		survival_time_label.text = "Survival Time: " + time_string
		
	if final_xp_label:
		final_xp_label.text = "Final XP: " + str(final_xp) + " XP"
		
	# نمایش بهترین رکورد ثبت شده از فایل JSON محلی
	if best_time_label:
		var best_min = int(SaveManager.best_survival_time) / 60
		var best_sec = int(SaveManager.best_survival_time) % 60
		best_time_label.text = "Personal Best: %02d:%02d" % [best_min, best_sec]

func _on_restart_pressed() -> void:
	get_tree().paused = false
	GameManager.start_game()
	get_tree().reload_current_scene()""".replace("#DOLLAR#", "$")
            )
        )
    }

    // Filter file items based on query
    val filteredFiles = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            godotFiles
        } else {
            godotFiles.filter {
                it.fileName.contains(searchQuery, ignoreCase = true) ||
                it.farsiTitle.contains(searchQuery) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg)
    ) {
        // App top header with godot themed brand
        HeaderSection()

        // Tab selection bar
        TabSwitcher(activeTab = activeTab) { activeTab = it }

        // Content Area depending on Tab
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (activeTab) {
                "explorer" -> {
                    ProjectExplorerLayout(
                        godotFiles = filteredFiles,
                        selectedIndex = selectedFileIndex,
                        onFileSelected = { selectedFileIndex = it },
                        searchQuery = searchQuery,
                        onSearchChanged = { searchQuery = it },
                        onCopy = { code ->
                            clipboardManager.setText(AnnotatedString(code))
                            Toast.makeText(context, "کد اسکریپت با موفقیت کپی شد", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                "sandbox" -> {
                    ResourcePlaygroundLayout(
                        charName = charName,
                        onCharNameChange = { charName = it },
                        charMaxHp = charMaxHp,
                        onCharMaxHpChange = { charMaxHp = it },
                        charSpeed = charSpeed,
                        onCharSpeedChange = { charSpeed = it },
                        charDefense = charDefense,
                        onCharDefenseChange = { charDefense = it },
                        weapName = weapName,
                        onWeapNameChange = { weapName = it },
                        weapDamage = weapDamage,
                        onWeapDamageChange = { weapDamage = it },
                        weapFireRate = weapFireRate,
                        onWeapFireRateChange = { weapFireRate = it },
                        weapProjSpeed = weapProjSpeed,
                        onWeapProjSpeedChange = { weapProjSpeed = it },
                        waveNum = waveNum,
                        onWaveNumChange = { waveNum = it },
                        waveInterval = waveInterval,
                        onWaveIntervalChange = { waveInterval = it },
                        waveDamage = waveDamage,
                        onWaveDamageChange = { waveDamage = it },
                        waveEnemyHp = waveEnemyHp,
                        onWaveEnemyHpChange = { waveEnemyHp = it },
                        waveEnemyCount = waveEnemyCount,
                        onWaveEnemyCountChange = { waveEnemyCount = it },
                        simXp = simXp,
                        onSimXpChange = { simXp = it },
                        simLevel = simLevel,
                        onSimLevelChange = { simLevel = it },
                        showUpgradeDialog = showUpgradeDialog,
                        onShowUpgradeDialogChange = { showUpgradeDialog = it },
                        survivalTime = survivalTime,
                        onSurvivalTimeChange = { survivalTime = it },
                        totalCoinsCollected = totalCoinsCollected,
                        onTotalCoinsCollectedChange = { totalCoinsCollected = it },
                        bestSurvivalTime = bestSurvivalTime,
                        onBestSurvivalTimeChange = { bestSurvivalTime = it },
                        unlockedItemsList = unlockedItemsList,
                        onUnlockedItemsListChange = { unlockedItemsList = it },
                        showGameOverDialog = showGameOverDialog,
                        onShowGameOverDialogChange = { showGameOverDialog = it },
                        onCopy = { code ->
                            clipboardManager.setText(AnnotatedString(code))
                            Toast.makeText(context, "کد ریسورس با موفقیت کپی شد", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                "architecture" -> {
                    ArchitectureMapLayout()
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GodotBlue.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status Tags
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF81C784))
                    )
                    Text(
                        text = "کد ساختار آماده دانلود است",
                        color = Color(0xFFD8DEE9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "آفلاین",
                        tint = GodotBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "کاملاً آفلاین و فاقد وابستگی خارجی",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Title + engine icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Godot 4 Bullet-Heaven Hub",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "معماری پایه بازی با گودواسکریپت",
                        color = GodotOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GodotBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Godot Icon",
                        tint = GodotBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TabSwitcher(activeTab: String, onTabSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SlateCard.copy(alpha = 0.5f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab 1: Code Explorer
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onTabSelected("explorer") }
                .background(
                    if (activeTab == "explorer") GodotBlue.copy(alpha = 0.15f) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Explorer",
                    tint = if (activeTab == "explorer") GodotBlue else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "مرورگر پروژه",
                    color = if (activeTab == "explorer") GodotBlue else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .width(1.dp),
            color = Color.DarkGray
        )

        // Tab 2: Sandbox config
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onTabSelected("sandbox") }
                .background(
                    if (activeTab == "sandbox") GodotOrange.copy(alpha = 0.15f) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Sandbox",
                    tint = if (activeTab == "sandbox") GodotOrange else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "شبیه‌ساز (Sandbox)",
                    color = if (activeTab == "sandbox") GodotOrange else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .width(1.dp),
            color = Color.DarkGray
        )

        // Tab 3: Architecture Guide (Step 3)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onTabSelected("architecture") }
                .background(
                    if (activeTab == "architecture") Color(0xFFC792EA).copy(alpha = 0.15f) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Architecture",
                    tint = if (activeTab == "architecture") Color(0xFFC792EA) else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "معماری گام ۳",
                    color = if (activeTab == "architecture") Color(0xFFC792EA) else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.ProjectExplorerLayout(
    godotFiles: List<GodotFileItem>,
    selectedIndex: Int,
    onFileSelected: (Int) -> Unit,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    // 1. Resolve Active File Details first
    val activeFile = if (selectedIndex >= 0 && selectedIndex < godotFiles.size) {
        godotFiles[selectedIndex]
    } else if (godotFiles.isNotEmpty()) {
        godotFiles[0]
    } else {
        null
    }

    // Left Column: Active File Details and actual Script Preview (Takes 3.2f weight)
    if (activeFile != null) {
        Card(
            modifier = Modifier
                .weight(3.2f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Header of details card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Copy and Path details
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = activeFile.virtualPath,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = { onCopy(activeFile.codeContent) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Copy Code",
                                tint = GodotBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Right: Title (Persian)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = activeFile.farsiTitle,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "نوع معماری: " + activeFile.category,
                            color = GodotOrange,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // Persian description block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = activeFile.farsiDescription,
                        color = Color(0xFFD8DEE9),
                        fontSize = 11.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Features lists
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Properties card (Left)
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SlateBg.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "متغیرها (Properties)",
                                color = GodotOrange,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            activeFile.propertiesList.forEach { prop ->
                                Text(
                                    text = "• $prop",
                                    color = Color.LightGray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Signals / Methods Card (Right)
                    Card(
                        modifier = Modifier.weight(1.2f),
                        colors = CardDefaults.cardColors(containerColor = SlateBg.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "توابع و سیگنال‌ها (Methods)",
                                color = GodotBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            activeFile.methodsList.forEach { met ->
                                Text(
                                    text = "⚡ $met",
                                    color = Color.LightGray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Code Viewer Window (Scrollable GDScript skeleton)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF14171E))
                        .border(1.dp, Color.DarkGray.copy(alpha = 0.4f))
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .horizontalScroll(rememberScrollState())
                            .padding(10.dp)
                    ) {
                        Text(
                            text = activeFile.codeContent,
                            color = Color(0xFFA3BE8C),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Right Column: Navigation / File List (Takes 1.8f weight)
    Column(
        modifier = Modifier
            .weight(1.8f)
            .fillMaxHeight()
    ) {
        // Search Input with Persian label
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            placeholder = {
                Text(
                    "جستجوی فایل‌ها (نام، طبقه‌بندی)...",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "جستجو",
                    tint = Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GodotBlue,
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = SlateCard,
                unfocusedContainerColor = SlateCard
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // File items
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(godotFiles) { file ->
                val currIndex = godotFiles.indexOf(file)
                val isSelected = currIndex == selectedIndex
                val fileCategoryColor = when (file.category) {
                    "Autoload" -> GodotOrange
                    "Resource" -> Color(0xFFC792EA)
                    "Scene" -> Color(0xFF82B1FF)
                    "Script" -> Color(0xFF71C6A0)
                    else -> GodotBlue
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFileSelected(currIndex) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SlateCard else SlateCard.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) GodotBlue.copy(alpha = 0.6f) else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Folder/category representation icon
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(fileCategoryColor.copy(alpha = 0.15f))
                                .padding(vertical = 2.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = file.category,
                                color = fileCategoryColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Info details (Persian right side)
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = file.fileName,
                                color = if (isSelected) TextPrimary else TextPrimary.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = file.farsiTitle,
                                color = if (isSelected) GodotOrange else Color.Gray,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Right
                            )
                        }

                        Icon(
                            imageVector = if (file.category == "Autoload" || file.category == "Core") Icons.Default.Info else Icons.Default.Menu,
                            contentDescription = "نوع فایل",
                            tint = if (isSelected) fileCategoryColor else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.ResourcePlaygroundLayout(
    charName: String,
    onCharNameChange: (String) -> Unit,
    charMaxHp: Float,
    onCharMaxHpChange: (Float) -> Unit,
    charSpeed: Float,
    onCharSpeedChange: (Float) -> Unit,
    charDefense: Float,
    onCharDefenseChange: (Float) -> Unit,
    weapName: String,
    onWeapNameChange: (String) -> Unit,
    weapDamage: Float,
    onWeapDamageChange: (Float) -> Unit,
    weapFireRate: Float,
    onWeapFireRateChange: (Float) -> Unit,
    weapProjSpeed: Float,
    onWeapProjSpeedChange: (Float) -> Unit,
    waveNum: Int,
    onWaveNumChange: (Int) -> Unit,
    waveInterval: Float,
    onWaveIntervalChange: (Float) -> Unit,
    waveDamage: Float,
    onWaveDamageChange: (Float) -> Unit,
    waveEnemyHp: Float,
    onWaveEnemyHpChange: (Float) -> Unit,
    waveEnemyCount: Int,
    onWaveEnemyCountChange: (Int) -> Unit,
    simXp: Float,
    onSimXpChange: (Float) -> Unit,
    simLevel: Int,
    onSimLevelChange: (Int) -> Unit,
    showUpgradeDialog: Boolean,
    onShowUpgradeDialogChange: (Boolean) -> Unit,
    survivalTime: Float,
    onSurvivalTimeChange: (Float) -> Unit,
    totalCoinsCollected: Int,
    onTotalCoinsCollectedChange: (Int) -> Unit,
    bestSurvivalTime: Float,
    onBestSurvivalTimeChange: (Float) -> Unit,
    unlockedItemsList: List<String>,
    onUnlockedItemsListChange: (List<String>) -> Unit,
    showGameOverDialog: Boolean,
    onShowGameOverDialogChange: (Boolean) -> Unit,
    onCopy: (String) -> Unit
) {
    val context = LocalContext.current
    var joystickDeltaX by remember { mutableFloatStateOf(0f) }
    var joystickDeltaY by remember { mutableFloatStateOf(0f) }

    // Live Simulator Coordinate Classes
    class SimEnemy(val id: Int, var x: Float, var y: Float, var hp: Float, val maxHp: Float)
    class SimProjectile(var x: Float, var y: Float, val vx: Float, val vy: Float)
    class SimXpGem(val x: Float, val y: Float, val xp: Float)
    class SimCoin(val x: Float, val y: Float)

    var playerX by remember { mutableFloatStateOf(160f) }
    var playerY by remember { mutableFloatStateOf(110f) }
    var simPlayerHp by remember { mutableFloatStateOf(charMaxHp) }
    var shootCooldownTimer by remember { mutableFloatStateOf(0f) }

    var simEnemies by remember { mutableStateOf(listOf<SimEnemy>()) }
    var simProjectiles by remember { mutableStateOf(listOf<SimProjectile>()) }
    var simGems by remember { mutableStateOf(listOf<SimXpGem>()) }
    var simCoinsList by remember { mutableStateOf(listOf<SimCoin>()) }

    LaunchedEffect(charMaxHp) {
        simPlayerHp = charMaxHp
    }

    LaunchedEffect(showUpgradeDialog, showGameOverDialog) {
        
        // Spawn active elements if list is empty
        if (simEnemies.isEmpty()) {
            simEnemies = listOf(
                SimEnemy(1, 40f, 40f, waveEnemyHp, waveEnemyHp),
                SimEnemy(2, 285f, 45f, waveEnemyHp, waveEnemyHp),
                SimEnemy(3, 285f, 175f, waveEnemyHp, waveEnemyHp)
            )
        }
        if (simGems.isEmpty()) {
            simGems = listOf(
                SimXpGem(110f, 120f, 25f),
                SimXpGem(210f, 85f, 25f),
                SimXpGem(150f, 145f, 25f)
            )
        }
        if (simCoinsList.isEmpty()) {
            simCoinsList = listOf(
                SimCoin(85f, 55f),
                SimCoin(235f, 135f)
            )
        }

        while (!showUpgradeDialog && !showGameOverDialog) {
            delay(33L) // ~30 FPS
            
            // 1. Tick up survival time slightly if game is active
            onSurvivalTimeChange(survivalTime + 0.033f)

            // 2. Move Player Knight by joystick
            if (joystickDeltaX != 0f || joystickDeltaY != 0f) {
                val speedScale = (charSpeed * 0.033f) * 0.14f
                playerX = (playerX + (joystickDeltaX / 20f) * speedScale).coerceIn(15f, 310f)
                playerY = (playerY + (joystickDeltaY / 20f) * speedScale).coerceIn(25f, 185f)
            }

            // 3. Shoot projectile towards nearest enemy based on rate of fire
            shootCooldownTimer -= 33f
            if (shootCooldownTimer <= 0f && simEnemies.isNotEmpty()) {
                val nearest = simEnemies.minByOrNull { (it.x - playerX) * (it.x - playerX) + (it.y - playerY) * (it.y - playerY) }
                if (nearest != null) {
                    val dx = nearest.x - playerX
                    val dy = nearest.y - playerY
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (dist > 0) {
                        val vx = (dx / dist) * 7f
                        val vy = (dy / dist) * 7f
                        simProjectiles = simProjectiles + SimProjectile(playerX, playerY, vx, vy)
                        shootCooldownTimer = 1000f / weapFireRate
                    }
                }
            }

            // 4. Update projectiles
            val updatedProjectiles = mutableListOf<SimProjectile>()
            simProjectiles.forEach { p ->
                p.x += p.vx
                p.y += p.vy
                
                if (p.x in 8f..320f && p.y in 15f..195f) {
                    var hit = false
                    val collidedEnemy = simEnemies.firstOrNull { e ->
                        val edx = e.x - p.x
                        val edy = e.y - p.y
                        edx * edx + edy * edy < 144f // ~12dp radius
                    }
                    
                    if (collidedEnemy != null) {
                        collidedEnemy.hp -= weapDamage
                        hit = true
                        if (collidedEnemy.hp <= 0f) {
                            // Enemy dies! Spawn gem
                            simGems = simGems + SimXpGem(collidedEnemy.x, collidedEnemy.y, 25f)
                            if (kotlin.random.Random.nextFloat() < 0.40f) {
                                simCoinsList = simCoinsList + SimCoin(collidedEnemy.x, collidedEnemy.y + 10f)
                            }
                            
                            // Respawn enemy at random boundary
                            collidedEnemy.x = if (kotlin.random.Random.nextBoolean()) 10f else 310f
                            collidedEnemy.y = kotlin.random.Random.nextFloat() * 140f + 35f
                            collidedEnemy.hp = waveEnemyHp
                        }
                    }
                    if (!hit) {
                        updatedProjectiles.add(p)
                    }
                }
            }
            simProjectiles = updatedProjectiles

            // 5. Update enemies (drift to player & attack)
            val updatedEnemies = simEnemies.map { e ->
                val edx = playerX - e.x
                val edy = playerY - e.y
                val dist = kotlin.math.sqrt(edx * edx + edy * edy)
                if (dist > 8f) {
                    e.x += (edx / dist) * 0.95f
                    e.y += (edy / dist) * 0.95f
                }
                
                if (dist < 18f) {
                    val damageTaken = waveDamage * 0.033f
                    val actualDamage = kotlin.math.max(0.1f, damageTaken - (charDefense * 0.004f))
                    simPlayerHp = (simPlayerHp - actualDamage).coerceAtLeast(0f)
                    if (simPlayerHp <= 0f) {
                        if (survivalTime > bestSurvivalTime) {
                            onBestSurvivalTimeChange(survivalTime)
                        }
                        onShowGameOverDialogChange(true)
                    }
                }
                e
            }
            simEnemies = updatedEnemies

            // 6. Update Gems (magnetic attraction & collect)
            val updatedGems = mutableListOf<SimXpGem>()
            simGems.forEach { gem ->
                val gdx = playerX - gem.x
                val gdy = playerY - gem.y
                val dist = kotlin.math.sqrt(gdx * gdx + gdy * gdy)
                
                if (dist < 55f) {
                    val nx = gem.x + (gdx / dist) * 3.5f
                    val ny = gem.y + (gdy / dist) * 3.5f
                    if (dist < 12f) {
                        val nextXp = simXp + gem.xp
                        if (nextXp >= 100f) {
                            onSimXpChange(nextXp % 100f)
                            onSimLevelChange(simLevel + 1)
                            onShowUpgradeDialogChange(true)
                        } else {
                            onSimXpChange(nextXp)
                        }
                    } else {
                        updatedGems.add(SimXpGem(nx, ny, gem.xp))
                    }
                } else {
                    updatedGems.add(gem)
                }
            }
            simGems = updatedGems

            // 7. Update Coins
            val updatedCoins = mutableListOf<SimCoin>()
            simCoinsList.forEach { coin ->
                val cdx = playerX - coin.x
                val cdy = playerY - coin.y
                val dist = kotlin.math.sqrt(cdx * cdx + cdy * cdy)
                if (dist < 12f) {
                    onTotalCoinsCollectedChange(totalCoinsCollected + 5)
                } else {
                    updatedCoins.add(coin)
                }
            }
            simCoinsList = updatedCoins
        }
    }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            containerColor = Color(0xFF2E3440),
            title = {
                Text(
                    text = "🎉 تراز جدید! انتخاب کارت ارتقا (Level Up)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "بازی متوقف شد! بر اساس منابع طراح بازی گودو، ارتقاهای زیر قرعه‌کشی شده‌اند. یکی را برای اعمال مستقیم به ویژگی‌های بازیکن انتخاب کنید:",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Weapon Damage
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onWeapDamageChange(weapDamage + 15f)
                                onSimXpChange(simXp - 100f)
                                onSimLevelChange(simLevel + 1)
                                onShowUpgradeDialogChange(false)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B4252)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E9F0).copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("دمیج: +15 واحد", color = Color(0xFFA3BE8C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text("💥 شمشیر بُران (Heavy Strike)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("صدمه ضربه پایه سلاح مجهز سنگین‌تر می‌شود.", color = Color.LightGray, fontSize = 10.sp, textAlign = TextAlign.Right)
                            }
                        }
                    }

                    // Option 2: Attack Speed
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onWeapFireRateChange(weapFireRate + 0.5f)
                                onSimXpChange(simXp - 100f)
                                onSimLevelChange(simLevel + 1)
                                onShowUpgradeDialogChange(false)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B4252)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E9F0).copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("سرعت شلیک: +0.5", color = Color(0xFFEBCB8B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text("🏹 آتشبار سریع (Rapid Fire Rate)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("نرخ شلیک و کوبش خودکار پرتابه‌ها بهبود می‌یابد.", color = Color.LightGray, fontSize = 10.sp, textAlign = TextAlign.Right)
                            }
                        }
                    }

                    // Option 3: Character Speed
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCharSpeedChange(charSpeed + 35f)
                                onSimXpChange(simXp - 100f)
                                onSimLevelChange(simLevel + 1)
                                onShowUpgradeDialogChange(false)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B4252)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E9F0).copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("سرعت حرکت: +35", color = Color(0xFF81A1C1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text("👟 چکمه جیوه‌ای (Ranger Swift Boots)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("جابجایی فیزیکی شوالیه روی نقشه سریع‌تر می‌شود.", color = Color.LightGray, fontSize = 10.sp, textAlign = TextAlign.Right)
                            }
                        }
                    }
                }
            }
        )
    }

    if (showGameOverDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            containerColor = Color(0xFF2E3440),
            title = {
                Text(
                    text = "💀 پایان بازی - شوالیه از پا درآمد (Game Over)",
                    color = Color(0xFFBF616A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "جنگجوی دلیر در این غائله سقوط کرد! آمارهای رزمی نهایی بر روی کارت باخت ثبت شده و در SaveManager محلی به صورت فرمت JSON نیز ماندگار شدند:",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Final survival time and coins stats
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B4252)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFBF616A).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val minutes = survivalTime.toInt() / 60
                                val seconds = survivalTime.toInt() % 60
                                Text(
                                    text = String.format("%02d:%02d", minutes, seconds),
                                    color = Color(0xFFEBCB8B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("⏱️ زمان بقا:", color = Color.White, fontSize = 11.sp)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${(simXp + simLevel * 100).toInt()} XP",
                                    color = Color(0xFF88C0D0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("✨ کل تجربه (Final XP):", color = Color.White, fontSize = 11.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$totalCoinsCollected سکه",
                                    color = Color(0xFFEBCB8B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("🪙 سکه‌های جمع‌آوری شده:", color = Color.White, fontSize = 11.sp)
                            }

                            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.4f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val bestMin = bestSurvivalTime.toInt() / 60
                                val bestSec = bestSurvivalTime.toInt() % 60
                                Text(
                                    text = String.format("%02d:%02d", bestMin, bestSec),
                                    color = Color(0xFFA3BE8C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("🏆 بهترین زمان بقا (Best):", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Restart button
                    Button(
                        onClick = {
                            // Simulator Restart
                            onSimXpChange(0f)
                            onSimLevelChange(1)
                            onSurvivalTimeChange(0f)
                            onTotalCoinsCollectedChange(0)
                            onShowGameOverDialogChange(false)
                            simPlayerHp = charMaxHp
                            playerX = 160f
                            playerY = 110f
                            simEnemies = listOf(
                                SimEnemy(1, 40f, 40f, waveEnemyHp, waveEnemyHp),
                                SimEnemy(2, 285f, 45f, waveEnemyHp, waveEnemyHp),
                                SimEnemy(3, 285f, 175f, waveEnemyHp, waveEnemyHp)
                            )
                            simGems = listOf(
                                SimXpGem(110f, 120f, 25f),
                                SimXpGem(210f, 85f, 25f),
                                SimXpGem(150f, 145f, 25f)
                            )
                            simCoinsList = listOf(
                                SimCoin(85f, 55f),
                                SimCoin(235f, 135f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA3BE8C)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text(
                            text = "🔄 شروع مجدد مبارزه (Restart)",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        )
    }

    // Controls (Left: 2.2f weight)
    Column(
        modifier = Modifier
            .weight(2.2f)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header explanation
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "توضیح: در گودو بجای هاردکد کردن مقادیر بازیکن و سلاح در خود کدها، آن‌ها را در فایل‌های جداگانه با پسوند tres ذخیره می‌کنند. با تغییر متغیرهای زیر، ساختار متنی ریسورس را در کادر روبرو به طور بلادرنگ ببیند.",
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(10.dp)
            )
        }

        // Hero config Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "👤 مشخصات منبع قهرمان (Hero Resource)",
                    color = GodotBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Hero name input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = charName,
                        onValueChange = onCharNameChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        textStyle = TextStyle(fontSize = 11.sp, textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GodotBlue,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Text("نام قهرمان: ", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Slider Hp
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = charMaxHp.toInt().toString(), fontSize = 11.sp, color = GodotBlue, fontWeight = FontWeight.Bold)
                    Slider(
                        value = charMaxHp,
                        onValueChange = onCharMaxHpChange,
                        valueRange = 50f..300f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = " حداکثر جان", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }

                // Slider Speed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = charSpeed.toInt().toString(), fontSize = 11.sp, color = GodotBlue, fontWeight = FontWeight.Bold)
                    Slider(
                        value = charSpeed,
                        onValueChange = onCharSpeedChange,
                        valueRange = 100f..400f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "سرعت حرکت", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }

                // Slider Defense
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = charDefense.toInt().toString(), fontSize = 11.sp, color = GodotBlue, fontWeight = FontWeight.Bold)
                    Slider(
                        value = charDefense,
                        onValueChange = onCharDefenseChange,
                        valueRange = 0f..30f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "قدرت دفاع", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }
            }
        }

        // Weapon spec card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "⚔️ مشخصات منبع سلاح (Weapon Resource)",
                    color = GodotOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Weapon name input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weapName,
                        onValueChange = onWeapNameChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        textStyle = TextStyle(fontSize = 11.sp, textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GodotOrange,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Text("نام سلاح: ", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Slider Damage
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = weapDamage.toInt().toString(), fontSize = 11.sp, color = GodotOrange, fontWeight = FontWeight.Bold)
                    Slider(
                        value = weapDamage,
                        onValueChange = onWeapDamageChange,
                        valueRange = 5f..150f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "صدمه ضربه", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }

                // Slider Rate of fire
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = String.format("%.1f", weapFireRate), fontSize = 11.sp, color = GodotOrange, fontWeight = FontWeight.Bold)
                    Slider(
                        value = weapFireRate,
                        onValueChange = onWeapFireRateChange,
                        valueRange = 0.5f..5.0f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "نرخ شلیک", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }
            }
        }

        // Wave settings card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "👾 منبع داده مشخصات موج فعال (Wave Info)",
                    color = Color(0xFFC792EA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Wave Index input counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { if (waveNum > 1) onWaveNumChange(waveNum - 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "کاهش موج", tint = Color.LightGray)
                        }
                        Text(
                            text = "موج $waveNum",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { onWaveNumChange(waveNum + 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "افزایش موج", tint = Color.LightGray)
                        }
                    }
                    Text("شناسه موج بقاء: ", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Slider Spawn Interval
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = String.format("%.1f ثانیه", waveInterval), fontSize = 11.sp, color = Color(0xFFC792EA), fontWeight = FontWeight.Bold)
                    Slider(
                        value = waveInterval,
                        onValueChange = onWaveIntervalChange,
                        valueRange = 0.5f..8.0f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "فاصله تولید", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }

                // Slider Wave Damage
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = waveDamage.toInt().toString(), fontSize = 11.sp, color = Color(0xFFC792EA), fontWeight = FontWeight.Bold)
                    Slider(
                        value = waveDamage,
                        onValueChange = onWaveDamageChange,
                        valueRange = 1f..120f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "دمیج زامبی", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }

                // Slider Enemy Max HP
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = waveEnemyHp.toInt().toString(), fontSize = 11.sp, color = Color(0xFFC792EA), fontWeight = FontWeight.Bold)
                    Slider(
                        value = waveEnemyHp,
                        onValueChange = onWaveEnemyHpChange,
                        valueRange = 10f..400f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "جان زامبی", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }

                // Slider Enemy Spawn Count on Wave
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = waveEnemyCount.toString(), fontSize = 11.sp, color = Color(0xFFC792EA), fontWeight = FontWeight.Bold)
                    Slider(
                        value = waveEnemyCount.toFloat(),
                        onValueChange = { onWaveEnemyCountChange(it.toInt()) },
                        valueRange = 5f..100f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "تعداد دشمن", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }
            }
        }

        // XP & Level-up Simulator Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFC792EA).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFC792EA).copy(alpha = 0.2f))
                            .padding(vertical = 2.dp, horizontal = 6.dp)
                    ) {
                        Text(
                            text = "تراز $simLevel",
                            color = Color(0xFFC792EA),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "✨ شبیه‌ساز زنده لول‌آپ و بلور XP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "با از بین رفتن زامبی‌ها بلور تجربه ساطع لایه‌ای شده و پس از جذب، بازی متوقف شده و ۳ کارت انتخابی (Upgrade Resource) بر اساس الگوهای گودو ظاهر می‌شود.",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // XP Progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${simXp.toInt()}/100 XP",
                        color = Color(0xFF88C0D0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "نوار پیشرفت تجربه (XP Base):",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { simXp / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF88C0D0),
                    trackColor = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onSimXpChange(0f)
                            onSimLevelChange(1)
                            simPlayerHp = charMaxHp
                            playerX = 160f
                            playerY = 110f
                            simEnemies = listOf(
                                SimEnemy(1, 40f, 40f, waveEnemyHp, waveEnemyHp),
                                SimEnemy(2, 285f, 45f, waveEnemyHp, waveEnemyHp),
                                SimEnemy(3, 285f, 175f, waveEnemyHp, waveEnemyHp)
                            )
                            simGems = listOf(
                                SimXpGem(110f, 120f, 25f),
                                SimXpGem(210f, 85f, 25f),
                                SimXpGem(150f, 145f, 25f)
                            )
                            simCoinsList = listOf(
                                SimCoin(85f, 55f),
                                SimCoin(235f, 135f)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("ریست شبیه‌ساز", color = Color.LightGray, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val newXp = simXp + 25f
                            if (newXp >= 100f) {
                                onSimXpChange(newXp)
                                onShowUpgradeDialogChange(true)
                            } else {
                                onSimXpChange(newXp)
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC792EA)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("🔋 کشتن زامبی و کسب XP", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Local Save & Game Over Simulator Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFBF616A).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "💾 شبیه‌ساز ذخیره محلی و منوی GameOver",
                    color = Color(0xFFBF616A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "تغییر مقادیر و شبیه‌سازی بازی برای مشاهده تعامل با متدهای JSON اسکریپت SaveManager و نحوه انتقال به منوی GameOver باخت.",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Slider Survival Time
                val curMin = survivalTime.toInt() / 60
                val curSec = survivalTime.toInt() % 60
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format("%02d:%02d", curMin, curSec),
                        fontSize = 11.sp,
                        color = Color(0xFFEBCB8B),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = survivalTime,
                        onValueChange = onSurvivalTimeChange,
                        valueRange = 10f..600f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "زمان بقا", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }

                // Slider Coins
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = totalCoinsCollected.toString(),
                        fontSize = 11.sp,
                        color = Color(0xFFEBCB8B),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = totalCoinsCollected.toFloat(),
                        onValueChange = { onTotalCoinsCollectedChange(it.toInt()) },
                        valueRange = 0f..500f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "سکه کسب شده", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Right)
                }

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

                // Save buttons and Trigger GameOver
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (survivalTime > bestSurvivalTime) {
                                onBestSurvivalTimeChange(survivalTime)
                            }
                            Toast.makeText(context, "ذخیره تغییرات در فایل مجازی user://save.json موفقیت‌آمیز بود!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1.5f),
                        border = BorderStroke(1.dp, Color(0xFFA3BE8C)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("💾 ثبت ذخیره JSON", color = Color(0xFFA3BE8C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            // Update best time internally on death
                            if (survivalTime > bestSurvivalTime) {
                                onBestSurvivalTimeChange(survivalTime)
                            }
                            onShowGameOverDialogChange(true)
                        },
                        modifier = Modifier.weight(1.8f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF616A)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("☠️ شبیه‌سازی باخت شوالیه", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Live Generated Outputs (Right: 2.8f weight)
    Column(
        modifier = Modifier
            .weight(2.8f)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 📱 HUD Live Viewport Simulator Card (Godot simulation with reactive anchors, HP, XP, Joystick & LevelUp Modal)
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, GodotBlue.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF88C0D0).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "گودو ۴ فعال",
                            color = Color(0xFF88C0D0),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "📱 شبیه‌ساز زنده HUD بازی (Godot Viewport)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "جهت‌های حرکت، شتاب‌دهی جوی‌استیک شناور و دکمه‌های ۳ کارت ارتقا متصل لمسی را به صورت کاملاً زنده امتحان کنید:",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Simulated Mobile Screen box (19:9)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2E3440)) // Nord Dark Background Base
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(10.dp))
                ) {
                    // Live Game Canvas drawing
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Tiled background
                        drawRect(color = Color(0xFF1E232B))
                        
                        // Subtle grid lines
                        for (x in 0..canvasWidth.toInt() step 60) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.04f),
                                start = Offset(x.toFloat(), 0f),
                                end = Offset(x.toFloat(), canvasHeight),
                                strokeWidth = 1f
                            )
                        }
                        for (y in 0..canvasHeight.toInt() step 45) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.04f),
                                start = Offset(0f, y.toFloat()),
                                end = Offset(canvasWidth, y.toFloat()),
                                strokeWidth = 1f
                            )
                        }

                        // Draw Coins
                        simCoinsList.forEach { coin ->
                            drawCircle(
                                color = Color(0xFFEBCB8B),
                                radius = 3.5f.dp.toPx(),
                                center = Offset(coin.x.dp.toPx(), coin.y.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFFEBCB8B).copy(alpha = 0.25f),
                                radius = 5.5f.dp.toPx(),
                                center = Offset(coin.x.dp.toPx(), coin.y.dp.toPx())
                            )
                        }

                        // Draw XP Gems
                        simGems.forEach { gem ->
                            rotate(45f, pivot = Offset(gem.x.dp.toPx(), gem.y.dp.toPx())) {
                                drawRect(
                                    color = Color(0xFF88C0D0),
                                    topLeft = Offset((gem.x - 3.5f).dp.toPx(), (gem.y - 3.5f).dp.toPx()),
                                    size = Size(7.dp.toPx(), 7.dp.toPx())
                                )
                            }
                        }

                        // Draw Projectiles
                        simProjectiles.forEach { proj ->
                            drawCircle(
                                color = Color(0xFF88C0D0),
                                radius = 2.dp.toPx(),
                                center = Offset(proj.x.dp.toPx(), proj.y.dp.toPx())
                            )
                        }

                        // Draw Enemies
                        simEnemies.forEach { enemy ->
                            drawCircle(
                                color = Color(0xFFBF616A),
                                radius = 7.dp.toPx(),
                                center = Offset(enemy.x.dp.toPx(), enemy.y.dp.toPx())
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.2f.dp.toPx(),
                                center = Offset((enemy.x - 2.5f).dp.toPx(), (enemy.y - 1.5f).dp.toPx())
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.2f.dp.toPx(),
                                center = Offset((enemy.x + 2.5f).dp.toPx(), (enemy.y - 1.5f).dp.toPx())
                            )
                            
                            // Bar background
                            val barW = 14.dp.toPx()
                            val barH = 2.dp.toPx()
                            val barX = enemy.x.dp.toPx() - (barW / 2)
                            val barY = enemy.y.dp.toPx() - 10.dp.toPx()
                            drawRect(
                                color = Color.DarkGray,
                                topLeft = Offset(barX, barY),
                                size = Size(barW, barH)
                            )
                            drawRect(
                                color = Color(0xFFBF616A),
                                topLeft = Offset(barX, barY),
                                size = Size(barW * (enemy.hp / enemy.maxHp).coerceIn(0f, 1f), barH)
                            )
                        }

                        // Draw Player Knight
                        drawCircle(
                            color = Color(0xFF81A1C1),
                            radius = 9.dp.toPx(),
                            center = Offset(playerX.dp.toPx(), playerY.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFECEFF4),
                            radius = 5.5f.dp.toPx(),
                            center = Offset(playerX.dp.toPx(), playerY.dp.toPx())
                        )
                        // Sword rotating or pointing to nearest
                        val nearestEnemy = simEnemies.minByOrNull { (it.x - playerX) * (it.x - playerX) + (it.y - playerY) * (it.y - playerY) }
                        if (nearestEnemy != null) {
                            val dx = nearestEnemy.x - playerX
                            val dy = nearestEnemy.y - playerY
                            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                            if (dist > 0) {
                                val sx = playerX.dp.toPx() + (dx / dist) * 13.dp.toPx()
                                val sy = playerY.dp.toPx() + (dy / dist) * 13.dp.toPx()
                                drawLine(
                                    color = Color(0xFFEBCB8B),
                                    start = Offset(playerX.dp.toPx(), playerY.dp.toPx()),
                                    end = Offset(sx, sy),
                                    strokeWidth = 2.5f.dp.toPx()
                                )
                            }
                        }
                    }

                    // Safe Area simulated (Anchors container)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        // --- TOP HUD ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Health Bar (Nord Red Style Box Flat)
                            Column(modifier = Modifier.weight(1.2f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${simPlayerHp.toInt()}/${charMaxHp.toInt()} HP",
                                        color = Color(0xFFBF616A),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(text = "🩸 سلامتی قهرمان", color = Color.LightGray, fontSize = 7.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = { (simPlayerHp / charMaxHp).coerceIn(0f, 1f) },
                                    color = Color(0xFFBF616A),
                                    trackColor = Color(0xFF3B4252),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Timer Label (Counts up) & Level info
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.weight(1f)
                            ) {
                                val minutes = survivalTime.toInt() / 60
                                val seconds = survivalTime.toInt() % 60
                                Text(
                                    text = String.format("⏱️ %02d:%02d", minutes, seconds),
                                    color = Color(0xFFEBCB8B), // Gold Theme Color
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "🪙 سکه: $totalCoinsCollected",
                                    color = Color(0xFFEBCB8B),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // --- BOTTOM HUD ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LVL $simLevel",
                                    color = Color(0xFF88C0D0),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${simXp.toInt()} / 100 XP",
                                    color = Color(0xFF88C0D0),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { simXp / 100f },
                                color = Color(0xFF88C0D0), // Ice Blue style box fg
                                trackColor = Color(0xFF3B4252),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }

                        // --- LEFT FLOATING VIRTUAL JOYSTICK ---
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = 20.dp, start = 4.dp)
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B4252).copy(alpha = 0.7f))
                                .border(1.dp, Color(0xFF81A1C1).copy(alpha = 0.5f), CircleShape)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { },
                                        onDragEnd = {
                                            joystickDeltaX = 0f
                                            joystickDeltaY = 0f
                                        },
                                        onDragCancel = {
                                            joystickDeltaX = 0f
                                            joystickDeltaY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            joystickDeltaX = (joystickDeltaX + dragAmount.x).coerceIn(-20f, 20f)
                                            joystickDeltaY = (joystickDeltaY + dragAmount.y).coerceIn(-20f, 20f)
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Knob inside Joystick
                            Box(
                                modifier = Modifier
                                    .offset(joystickDeltaX.dp, joystickDeltaY.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (joystickDeltaX != 0f || joystickDeltaY != 0f) Color(0xFF88C0D0)
                                        else Color(0xFF81A1C1)
                                    )
                                    .shadow(2.dp, CircleShape)
                            )
                        }

                        // Small status label for Joystick outputs
                        if (joystickDeltaX != 0f || joystickDeltaY != 0f) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = String.format("Event: (%.2f, %.2f)", joystickDeltaX / 20f, -joystickDeltaY / 20f),
                                    color = Color(0xFFA3BE8C),
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // --- LEVEL-UP MODAL OVERLAY (SENSITIVE AREA) ---
                        if (showUpgradeDialog) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF2E3440).copy(alpha = 0.95f))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "✨ لول آپ! گزینش ارتقای تراز (Common Theme) ✨",
                                        color = Color(0xFFEBCB8B),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(3.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Card 1
                                        Button(
                                            onClick = {
                                                onSimLevelChange(simLevel + 1)
                                                onSimXpChange(0f)
                                                onTotalCoinsCollectedChange(totalCoinsCollected + 10)
                                                onShowUpgradeDialogChange(false)
                                                Toast.makeText(context, "ارتقای Damage +15% انتخاب شد!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B4252)),
                                            border = BorderStroke(1.dp, Color(0xFF88C0D0).copy(alpha = 0.3f)),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("⚔️ [Card 1]", color = Color(0xFFEBCB8B), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                Text("قدرت ضربه (Damage +15%)", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Card 2
                                        Button(
                                            onClick = {
                                                onSimLevelChange(simLevel + 1)
                                                onSimXpChange(0f)
                                                onTotalCoinsCollectedChange(totalCoinsCollected + 10)
                                                onShowUpgradeDialogChange(false)
                                                Toast.makeText(context, "ارتقای Attack Rate +10% انتخاب شد!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B4252)),
                                            border = BorderStroke(1.dp, Color(0xFF88C0D0).copy(alpha = 0.3f)),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("⚡ [Card 2]", color = Color(0xFFEBCB8B), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                Text("دوندگی باد (Speed +10%)", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Card 3
                                        Button(
                                            onClick = {
                                                onSimLevelChange(simLevel + 1)
                                                onSimXpChange(0f)
                                                onTotalCoinsCollectedChange(totalCoinsCollected + 10)
                                                onShowUpgradeDialogChange(false)
                                                Toast.makeText(context, "ارتقای Base Defense +5 انتخاب شد!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B4252)),
                                            border = BorderStroke(1.dp, Color(0xFF88C0D0).copy(alpha = 0.3f)),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🛡️ [Card 3]", color = Color(0xFFEBCB8B), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                Text("زره محافظتی (Defense +5)", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Stats and math estimations card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GodotOrange.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "📊 پیش‌بینی بالانس هوشمند (Real-Time Estimation)",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Calculate real DPS
                val dps = weapDamage * weapFireRate
                val powerIndex = (charMaxHp * (1f + charDefense / 10f) * charSpeed / 100f).toInt()
                val waveThreat = (waveEnemyHp * waveDamage / (waveInterval + 0.1f)).toInt()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "$powerIndex point", color = GodotBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = "شاخص بقای شوالیه:", color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = String.format("%.1f DPS", dps), color = GodotOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = "توان شلیک سلاح:", color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "$waveThreat point", color = Color(0xFFC792EA), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = "چگالی چالش موج $waveNum:", color = Color.LightGray, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(6.dp))

                val balanceText: String
                val balanceColor: Color
                if (powerIndex > waveThreat * 1.5) {
                    balanceText = "شوالیه بی‌رحم و آماده عبور تفریحی از موج بقاء! چالش کم است."
                    balanceColor = GodotBlue
                } else if (powerIndex < waveThreat * 0.7) {
                    balanceText = "موج مرگبار زامبی‌ها! خطر مرگ زودهنگام بسیار بالا است. سلاح را قوی‌تر کنید."
                    balanceColor = Color(0xFFE91E63)
                } else {
                    balanceText = "توازن عالی برقرار است! چالش شیرین و هاردکور سبک Bullet Heaven."
                    balanceColor = Color(0xFF4CAF50)
                }

                Text(
                    text = balanceText,
                    color = balanceColor,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Raw .tres Code output card representation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            // Generate resource, wave codes and save.json representations
            val generatedCode = """# --- res://resources/character_data_1.tres ---
[gd_resource type="Resource" script_class="CharacterData" load_steps=2 format=3]
[ext_resource type="Script" path="res://resources/character_data.gd" id="1_char"]

[resource]
script = ExtResource("1_char")
character_name = "$charName"
max_health = ${charMaxHp.toInt()}.0
base_speed = ${charSpeed.toInt()}.0
defense = ${charDefense.toInt()}.0
life_steal_pct = 0.0

# --- res://resources/weapon_data_1.tres ---
[gd_resource type="Resource" script_class="WeaponData" load_steps=2 format=3]
[ext_resource type="Script" path="res://resources/weapon_data.gd" id="1_weap"]

[resource]
script = ExtResource("1_weap")
weapon_name = "$weapName"
base_damage = ${weapDamage.toInt()}.0
fire_rate = ${String.format("%.1f", weapFireRate)}
projectile_speed = ${weapProjSpeed.toInt()}.0

# --- res://resources/waves/wave_$waveNum.tres ---
[gd_resource type="Resource" script_class="WaveData" load_steps=2 format=3]
[ext_resource type="Script" path="res://resources/wave_data.gd" id="1_wave"]

[resource]
script = ExtResource("1_wave")
wave_index = $waveNum
spawn_interval = ${String.format("%.1f", waveInterval)}
enemy_damage = ${waveDamage.toInt()}.0
enemy_speed = 110.0
enemy_max_health = ${waveEnemyHp.toInt()}.0

# --- user://save.json (فایل ذخیره محلی JSON آفلاین) ---
{
	"best_survival_time": ${String.format("%.1f", bestSurvivalTime)},
	"coins_collected": $totalCoinsCollected,
	"unlocked_items": [
		${unlockedItemsList.joinToString(",\n\t\t") { "\"$it\"" }}
	]
}"""

            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onCopy(generatedCode) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Copy Generated Resource",
                            tint = GodotOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "کد منابع ساختار داده (.tres)",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .border(1.dp, Color.DarkGray.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    val scrollState = rememberScrollState()
                    Text(
                        text = generatedCode,
                        color = Color(0xFFD8DEE9),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    )
                }
            }
        }
    }
}

@Composable
fun ArchitectureMapLayout() {
    var selectedNode by remember { mutableStateOf("EventBus") }

    val nodes = listOf(
        ArchitectureNode("EventBus", "res://autoload/event_bus.gd", "رویداد پیشران هاب", "Autoload", GodotOrange, Icons.Default.Share),
        ArchitectureNode("GameManager", "res://autoload/game_manager.gd", "مدیر وضعیت و لوپ", "Autoload", GodotBlue, Icons.Default.Build),
        ArchitectureNode("SaveManager", "res://autoload/save_manager.gd", "نگهدارنده لول و رکوردها", "Autoload", Color(0xFFC792EA), Icons.Default.Star),
        ArchitectureNode("EnemyPool", "res://autoload/enemy_pool.gd", "بهینه‌ساز رم موبایل", "Autoload", Color(0xFFEBCB8B), Icons.Default.Refresh),
        ArchitectureNode("Player", "res://scenes/player/player.tscn", "کاراکتر اصلی مبارز", "Scene/Script", Color(0xFF81C784), Icons.Default.Person),
        ArchitectureNode("EnemyBase", "res://scenes/enemies/enemy_base.tscn", "دشمن تفنگ‌به‌دست / زامبی", "Scene/Script", Color(0xFFF06292), Icons.Default.Warning),
        ArchitectureNode("HUD/UI", "res://scenes/ui/hud.tscn", "کنتور زمان و جان و XP", "Scene/Script", Color(0xFF4FC3F7), Icons.Default.Info)
    )

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left Panel: Dynamic Details (Takes 2.2f weight)
        Card(
            modifier = Modifier
                .weight(2.2f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.End
            ) {
                val node = nodes.firstOrNull { it.id == selectedNode } ?: nodes[0]
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(node.color.copy(alpha = 0.15f))
                            .padding(vertical = 2.dp, horizontal = 6.dp)
                    ) {
                        Text(
                            text = node.category,
                            color = node.color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = node.id,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = node.subtitle,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = node.path,
                    color = GodotBlue,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                // Explain decoupling & events connection
                Text(
                    text = "🎯 عملکرد در معماری گام ۳ (بهینه‌سازی موبایل):",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                val detailText = when (node.id) {
                    "EventBus" -> """• این ماژول هسته اصلی جداکننده (Decoupler) در بازی است.
• در گام ۳، تمام رویدادها مانند صدمه دیدن، مرگ دشمن یا لول‌آپ به جای اتصال مستقیم کدهای درهم‌تنیده، از طریق EventBus توزیع می‌شوند.
• این کار باعث افزایش FPS روی دستگاه‌های ضعیف می‌شود زیرا بار سر بار ارجاعات سنگین گره‌ها به شدت کاهش می‌یابد."""
                    "GameManager" -> """• مدیریت زمان، امتیاز و سطح دشواری بازی در دست این جز سراسری است.
• در گام ۳، این هاب مستقیماً اطلاعات ذخیره شده توسط SaveManager را بارگذاری می‌کند و فلو بازی را کالیبره می‌کند.
• همچنین با اتمام زمان بقا، فریم ریت را برای جلوگیری از کرش بر روی موبایل فریز یا بهینه می‌نماید."""
                    "SaveManager" -> """• مدیریت پایداری محلی رکوردها و اطلاعات باز کردن قفل تفنگ‌ها به صورت آفلاین.
• در گام ۳، داده‌ها با ساختار امن دیکشنری فرمت شده و با استفاده از آبجکت FileAccess به مسیر اختصاصی موبایل یعنی user://offline_save.dat صادر می‌شود.
• ذخیره‌سازی همواره پس از رویداد بازی گام ۳ به طور موازی بدون مسدود کردن ترد رندرینگ اصلی انجام می‌شود."""
                    "EnemyPool" -> """• بحرانی‌ترین بهینه‌سازی گام ۳ برای موبایل: استفاده از آبجکت پولینگ (Object Pooling).
• به جای تخصیص مکرر حافظه و حذف (queue_free) زامبی‌ها که باعث لک زدن (Garbage Collection Stutter) روی اندروید می‌شود، دشمنان غیرفعال به استخر منتقل شده و دوباره به کار گرفته می‌شوند.
• این کار سربار سی‌پی‌یو را تا ۸۰ درصد روی موبایل کاهش می‌دهد."""
                    "Player" -> """• این صحنه متحرک شامل فیزیک، انیمیشن‌های برداری، و ورودی‌های لمسی موبایل است.
• در گام ۳، وضعیت بازیکن مستقیماً از ریسورس داده CharacterData استخراج شده و به هیچ وجه هاردکد نمی‌شود.
• حرکات بازیکن کاملاً با جوی‌استیک لمسی تک انگشتی ناشی از فیدبک سیگنال‌های MobileInput ترجمه و همگام گردیده است."""
                    "EnemyBase" -> """• دشمنان فیزیکی با الگوی هوش مصنوعی ساده جهت ردگیری بازیکن و ممانعت از کرش.
• در گام ۳، سرعت فیزیکی و توان ضرباتی با توجه به شماره موج (Wave Info) به صورت داده‌محور مقیاس‌بندی شده و دمیج ضربه‌ای را به صورت پالسی تحمیل می‌کنند."""
                    else -> """• نوار سلامتی بازیکن، تجربه، تراز جاری و رکورد زنده ماندن را منعکس می‌کند.
• در گام ۳، این بخش از طریق تم اشتراکی (UITheme) و با سبک متریال ۳ بدون لک بصری بروزرسانی می‌گردد.
• از آیکون‌های برداری بهینه برای کارایی ماکزیمم استفاده شده است."""
                }
                
                Text(
                    text = detailText,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                
                // Code block example for Step 3
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, Color.DarkGray.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    val sampleCode = when (node.id) {
                        "EventBus" -> """# res://autoload/event_bus.gd
signal player_died
signal xp_collected(amount: int)
signal game_over(score: int)"""
                        "EnemyPool" -> """# res://autoload/enemy_pool.gd
var pool: Array = []
func get_enemy():
    if pool.is_empty():
        return enemy_scene.instatiate()
    return pool.pop_back()"""
                        "SaveManager" -> """# res://autoload/save_manager.gd
func save_offline():
    var file = FileAccess.open(
        "user://save.dat", WRITE
    )
    file.store_var(data)"""
                        else -> "# Godot 4 Optimization\n# Fully decoupled signal-driven system"
                    }
                    Text(
                        text = sampleCode,
                        color = node.color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Right Panel: Interactive Visual Network (Takes 1.8f weight)
        Card(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🛠️ چیدمان گره‌های صحنه گام ۳",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw clean connected nodes
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        nodes.forEach { item ->
                            val isSelected = item.id == selectedNode
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedNode = item.id },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) item.color.copy(alpha = 0.25f) else SlateCard
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) item.color else Color.DarkGray.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.id,
                                        tint = if (isSelected) item.color else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                                    ) {
                                        Text(
                                            text = item.id,
                                            color = if (isSelected) Color.White else TextPrimary.copy(alpha = 0.8f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = item.subtitle,
                                            color = if (isSelected) item.color else Color.Gray,
                                            fontSize = 8.sp,
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                // Farsi advice
                Text(
                    text = "💡 روی هر بخش کلیک کنید تا رازهای بهینه‌سازی گودو ۴ روی موبایل را ببینید.",
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

data class ArchitectureNode(
    val id: String,
    val path: String,
    val subtitle: String,
    val category: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
