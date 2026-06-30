extends Node

# -------------------------------------------------------------
# EnemyPool - اتولود جدید برای مدیریت استخر شیء (Object Pooling) دشمنان
# نقش: ممانعت از ایجاد و حذف مکرر اشیاء فیزیکی در حافظه که در دستگاه‌های
# موبایل اندرویدی باعث افت فریم ریت شدید و لگ‌های لودینگ می‌گردد.
# -------------------------------------------------------------

# برای جلوگیری از خطای وابستگی چرخه‌ای با EnemyBase از load استفاده می‌کنیم
var enemy_scene: PackedScene

var pool: Array = []

func _ready() -> void:
	enemy_scene = load("res://scenes/enemies/enemy_base.tscn")
	# تولید زودهنگام تعدادی دشمن در ابتدای بازی جهت افزایش پرفورمنس
	for i in range(15):
		var enemy = _create_new_enemy()
		enemy.visible = false
		enemy.process_mode = PROCESS_MODE_DISABLED
		pool.append(enemy)

func _create_new_enemy() -> CharacterBody2D:
	var enemy = enemy_scene.instantiate() as CharacterBody2D
	add_child(enemy)
	return enemy

func get_enemy() -> CharacterBody2D:
	var enemy: CharacterBody2D
	
	# اگر استخر خالی نبود، آخرین گره غیرفعال را گرفته و دوباره فعال می‌کنیم
	if pool.size() > 0:
		enemy = pool.pop_back()
		while enemy != null and !is_instance_valid(enemy):
			if pool.size() > 0:
				enemy = pool.pop_back()
			else:
				enemy = null
				break
	
	# اگر دشمن نامعتبر بود یا استخر کاملا خالی شد، یکی تازه می‌سازیم
	if enemy == null:
		enemy = _create_new_enemy()
		
	enemy.visible = true
	enemy.process_mode = PROCESS_MODE_INHERIT
	
	# ریست کردن جان و حالت پایه دشمن به محض خروج از استخر
	if enemy.has_method("reset_state"):
		enemy.reset_state()
		
	return enemy

func return_to_pool(enemy: CharacterBody2D) -> void:
	if is_instance_valid(enemy):
		# غیرفعال‌سازی فیزیکی و بصری زامبی بجای حذف کامل از رم موبایل
		enemy.visible = false
		enemy.process_mode = PROCESS_MODE_DISABLED
		
		# حذف از صحنه والد تا تداخلی در محاسبات موقعیت پیش نیاید
		if enemy.get_parent():
			enemy.get_parent().remove_child(enemy)
			
		pool.append(enemy)
