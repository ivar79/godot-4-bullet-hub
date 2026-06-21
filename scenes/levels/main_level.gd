extends Node2D

# -------------------------------------------------------------
# MainLevel - مدیریت صحنه اصلی گیم‌پلی و مدیریت رویدادهای جریان بقا
# نقش: راه‌اندازی GameManager، گوش دادن فعال به رویدادها صادر شده در EventBus،
# تولید کریستال‌ها به عنوان درآپ دشمنان و باز کردن پنل بازخورد لول‌آپ و پایان بازی.
# -------------------------------------------------------------

func _ready() -> void:
	# اتصال به ایستگاه‌های فرستنده رویدادهای عمومی
	EventBus.connect("enemy_died", Callable(self, "_on_enemy_died"))
	EventBus.connect("player_level_up", Callable(self, "_on_player_level_up"))
	EventBus.connect("game_over", Callable(self, "_on_game_over"))
	
	# آغاز بازی بومی با شروع تایمر بقاء آفلاین
	GameManager.start_game()

func _on_enemy_died(enemy_node: CharacterBody2D, _score: int, xp_value: int) -> void:
	# رها کردن بلور درخشان تجربه (XP Gem) دقیقاً در نقطه مرگ دشمن
	var gem_scene = preload("res://scenes/items/xp_gem.tscn")
	var gem_instance = gem_scene.instantiate() as XPGem
	gem_instance.xp_value = xp_value
	gem_instance.global_position = enemy_node.global_position
	
	call_deferred("add_child", gem_instance)

func _on_player_level_up(new_level: int) -> void:
	# نمایش صحنه ارتقای قابلیت با به تاخیر انداختن پردازش زمان
	var lvlup_scene = preload("res://scenes/ui/level_up_panel.tscn")
	var lvlup_instance = lvlup_scene.instantiate()
	add_child(lvlup_instance)

func _on_game_over(final_score: int, time_survived: float) -> void:
	# متوقف کردن کل درخت فیزیک و نمایش کارت تسلیت باخت (GameOver)
	get_tree().paused = true
	var gameover_scene = preload("res://scenes/ui/game_over.tscn")
	var gameover_instance = gameover_scene.instantiate()
	add_child(gameover_instance)
