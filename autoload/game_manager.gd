extends Node

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
	EventBus.enemy_died.connect(_on_enemy_died)
	EventBus.player_died.connect(_on_player_died)

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
	EventBus.game_started.emit()

func pause_game(is_paused: bool) -> void:
	get_tree().paused = is_paused
	EventBus.game_paused.emit(is_paused)

func add_xp(amount: int) -> void:
	current_xp += amount
	if current_xp >= xp_to_next_level:
		level_up()
	else:
		EventBus.xp_collected.emit(amount, current_xp, xp_to_next_level)

func level_up() -> void:
	current_xp -= xp_to_next_level
	current_level += 1
	xp_to_next_level = int(xp_to_next_level * 1.5) # افزایش سختی تراز بعدی
	EventBus.player_level_up.emit(current_level)
	EventBus.xp_collected.emit(0, current_xp, xp_to_next_level)

func _on_enemy_died(_enemy_node: CharacterBody2D, score_value: int, xp_value: int) -> void:
	current_score += score_value
	add_xp(xp_value)

func _on_player_died() -> void:
	is_game_active = false
	# ذخیره خودکار امتیاز با SaveManager
	SaveManager.save_high_score(current_score)
	EventBus.game_over.emit(current_score, time_elapsed)
