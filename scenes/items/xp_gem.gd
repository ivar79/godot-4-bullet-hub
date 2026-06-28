class_name XPGem
extends Area2D

# -------------------------------------------------------------
# XPGem - بلور یا گوهر تجربه حاصل از مرگ زامبی‌ها
# نقش: رها شدن روی زمین، کشیده شدن مگنتی به سمت بازیکن در صورت نزدیکی
# و افزایش تراز تجربه (XP) در GameManager پس از لمس مستقیم بازیکن.
# -------------------------------------------------------------

@export var xp_value: int = 15
@export var magnet_radius: float = 120.0
@export var collect_radius: float = 18.0
@export var speed: float = 380.0

var player = null
var is_magnetized: bool = false

func _ready() -> void:
	add_to_group("gems")
	queue_redraw()
	var players = get_tree().get_nodes_in_group("player")
	if players.size() > 0:
		player = players[0]

func _process(delta: float) -> void:
	if player and is_instance_valid(player):
		var dist = global_position.distance_to(player.global_position)
		
		# فعال‌سازی کشش مغناطیسی (مکانیک بسیار جذاب و اعتیادآور روگ‌لایک)
		if dist <= magnet_radius or is_magnetized:
			is_magnetized = true
			# حرکت مستقیم و پرشتاب به سمت مغناطیس بازیکن
			var direction = (player.global_position - global_position).normalized()
			global_position += direction * speed * delta
			
			# افزایش تدریجی سرعت کشش برای حس بهتر بازی
			speed = min(speed + 12.0, 800.0)
			
		# بلعیده شدن توسط بازیکن و افزایش امتیاز تجربه
		if dist <= collect_radius:
			collect()

func collect() -> void:
	# غیرفعال کردن موقت پروسه جهت ممانعت از برخورد مضاعف
	set_process(false)
	
	# افکت مکش بصری در حال انقباض نهایی
	var tween = create_tween()
	tween.tween_property(self, "scale", Vector2.ZERO, 0.15)
	
	GameManager.add_xp(xp_value)
	
	tween.finished.connect(queue_free)

func _draw() -> void:
	# ترسیم یک لوزی درخشان بنفش/فیروزه‌ای به عنوان الماس تجربه بدون عیب بصری
	var points = PackedVector2Array([
		Vector2(0, -10),
		Vector2(7, 0),
		Vector2(0, 10),
		Vector2(-7, 0)
	])
	draw_polygon(points, [Color(0.7, 0.2, 1.0, 0.9)]) # بدنه الماس بنفش
	draw_polyline(points, Color.WHITE, 1.5)            # حاشیه درخشان سفید
