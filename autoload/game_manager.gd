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

# متغیرهای مربوط به سیستم مراحل (Stage System)
var current_stage: int = 1
var stage_kills: int = 0
var is_stage_completed: bool = false
var stage_mini_boss_kills: int = 0
var final_boss_spawned: bool = false

func _ready() -> void:
	# اتصال به رویدادهای عمومی در EventBus
	EventBus.enemy_died.connect(_on_enemy_died)
	EventBus.player_died.connect(_on_player_died)

func _process(delta: float) -> void:
	if is_game_active and not is_stage_completed:
		time_elapsed += delta
		_check_stage_victory()

func get_stage_duration() -> float:
	# مراحل ۱ تا ۴ نیاز به ۳ دقیقه (۱۸۰ ثانیه) دارند.
	# مراحل مضرب ۵ (مثلاً ۵، ۱۰، ۱۵، ۲۰ و ...) نیاز به ۵ دقیقه (۳۰۰ ثانیه) دارند.
	if current_stage % 5 == 0:
		return 300.0
	else:
		return 180.0

func start_game() -> void:
	is_game_active = true
	current_score = 0
	current_gold = 0
	current_level = 1
	current_xp = 0
	xp_to_next_level = 100
	time_elapsed = 0.0
	current_stage = SaveManager.max_completed_stage + 1
	stage_kills = 0
	stage_mini_boss_kills = 0
	final_boss_spawned = false
	is_stage_completed = false
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

func _check_stage_victory() -> void:
	if is_game_active and not is_stage_completed:
		var time_needed = get_stage_duration()
		if time_elapsed >= time_needed and not final_boss_spawned:
			_spawn_final_boss()

func _spawn_final_boss() -> void:
	final_boss_spawned = true
	EventBus.show_announcement.emit("⚠️ هشدار: پادشاه تاریکی ظاهر شد! او را نابود کنید تا مرحله پیروز شود! ⚠️", Color(1.0, 0.1, 0.1))
	EventBus.final_boss_spawned.emit()

func _complete_stage() -> void:
	is_stage_completed = true
	if current_stage > SaveManager.max_completed_stage:
		SaveManager.max_completed_stage = current_stage
		SaveManager.save_game()
		
	EventBus.stage_completed.emit(current_stage)
	
	# باز کردن دیالوگ شیک پایان موفقیت‌آمیز مرحله
	var clear_scene = load("res://scenes/ui/stage_clear_panel.tscn")
	if clear_scene:
		var panel_instance = clear_scene.instantiate()
		get_tree().current_scene.add_child(panel_instance)

func start_next_stage() -> void:
	current_stage += 1
	stage_kills = 0
	stage_mini_boss_kills = 0
	final_boss_spawned = false
	time_elapsed = 0.0
	is_stage_completed = false
	
	# بازگشت خاضعانه تمام دشمنان فعال به استخر حافظه برای شروع تمیز مرحله بعد
	for enemy in get_tree().get_nodes_in_group("enemies"):
		if is_instance_valid(enemy):
			EnemyPool.return_to_pool(enemy)
			
	# حذف تمام گلوله‌ها و الماس‌های مانده روی زمین
	for proj in get_tree().get_nodes_in_group("projectiles"):
		if is_instance_valid(proj):
			proj.queue_free()
			
	for gem in get_tree().get_nodes_in_group("gems"):
		if is_instance_valid(gem):
			gem.queue_free()
			
	# شفا بخشیدن کامل به قهرمان خسته به عنوان پاداش
	var player = get_tree().get_first_node_in_group("player")
	if player and is_instance_valid(player):
		player.heal(player.max_health)
		
	# انتشار رویداد برای تنظیم مجدد سختی اسپاونر و شروع مرحله نو
	EventBus.stage_started.emit(current_stage)
	
	get_tree().paused = false

func _on_enemy_died(enemy_node: CharacterBody2D, score_value: int, xp_value: int) -> void:
	current_score += score_value
	stage_kills += 1
	add_xp(xp_value)
	
	# رهگیری مینی‌باس‌ها و باس نهایی
	if is_instance_valid(enemy_node):
		if "is_boss" in enemy_node and enemy_node.is_boss:
			stage_mini_boss_kills += 1
			# نمایش اعلامیه در وسط صفحه
			EventBus.show_announcement.emit("⚔️ مینی‌باس شکست خورد! (" + str(stage_mini_boss_kills) + "/7) ⚔️", Color(0.9, 0.7, 0.1))
			# ارتقاء خاص کاراکتر
			var player = get_tree().get_first_node_in_group("player")
			if player and is_instance_valid(player) and player.has_method("check_character_boss_milestones"):
				player.check_character_boss_milestones(stage_mini_boss_kills)
				
		if "is_final_boss" in enemy_node and enemy_node.is_final_boss:
			EventBus.show_announcement.emit("👑 پادشاه تاریکی سقوط کرد! پیروزی نهایی! 👑", Color(0.1, 1.0, 0.4))
			_complete_stage()

func _on_player_died() -> void:
	is_game_active = false
	# ذخیره خودکار امتیاز با SaveManager
	SaveManager.save_high_score(current_score)
	EventBus.game_over.emit(current_score, time_elapsed)
