extends CanvasLayer

# -------------------------------------------------------------
# HUD - واسط گرافیکی بر روی صفحه (Heads-Up Display)
# نقش: نمایش میزان جان زنده ماندن بازیکن، لول یا تراز جاری،
# نوار پیشرفت کسب تجربه (XP) و نمایش ثانیه‌ها/دقیقه‌های زنده ماندن.
# همچنین رسم جوی‌استیک لمسی بر روی موبایل موقع جابجایی دست.
# -------------------------------------------------------------

@onready var hp_bar: ProgressBar = $Control/HPBar
@onready var xp_bar: ProgressBar = $Control/XPBar
@onready var score_label: Label = $Control/ScoreLabel
@onready var level_label: Label = $Control/LevelLabel
@onready var timer_label: Label = $Control/TimerLabel

func _ready() -> void:
	# اتصال به رویدادهای EventBus برای به‌روزرسانی آنی نماها
	EventBus.connect("player_health_changed", Callable(self, "_on_player_health_changed"))
	EventBus.connect("xp_collected", Callable(self, "_on_xp_collected"))
	EventBus.connect("player_level_up", Callable(self, "_on_player_level_up"))
	
	score_label.text = "Score: 0"
	level_label.text = "Lvl: 1"

func _process(_delta: float) -> void:
	if GameManager.is_game_active:
		timer_label.text = format_time(GameManager.time_elapsed)
		score_label.text = "Score: " + str(GameManager.current_score)

func format_time(time_in_seconds: float) -> String:
	var minutes = int(time_in_seconds) / 60
	var seconds = int(time_in_seconds) % 60
	return "%02d:%02d" % [minutes, seconds]

func _on_player_health_changed(current: float, max_val: float) -> void:
	hp_bar.max_value = max_val
	hp_bar.value = current

func _on_xp_collected(_amt: int, current: int, next_lvl_xp: int) -> void:
	xp_bar.max_value = next_lvl_xp
	xp_bar.value = current

func _on_player_level_up(new_level: int) -> void:
	level_label.text = "Lvl: " + str(new_level)

