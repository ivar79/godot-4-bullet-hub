extends Node2D

# -------------------------------------------------------------
# MainLevel - مدیریت صحنه اصلی گیم‌پلی و اسپون دشمنان
# نقش: لود کردن صحنه هاب گرافیکی بازیکن، راه‌اندازی تایمر تولید
# گروهی دشمنان دور تا دور صفحه دوربین، و فعال‌سازی بازی از طریق GameManager.
# -------------------------------------------------------------

@export var enemy_scene: PackedScene = preload("res://scenes/enemies/enemy_base.scn") if ResourceLoader.exists("res://scenes/enemies/enemy_base.scn") else null

@onready var spawn_timer: Timer = $SpawnTimer
@onready var player: Player = $Player

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
	add_child(enemy_instance)
