class_name Projectile
extends Area2D

# -------------------------------------------------------------
# Projectile - اسکریپت تیر / پرتابه شلیک شده توسط سلاح خودکار
# نقش: حرکت مستقل در جهتی مشخص، شناسایی دشمنان، وارد کردن دیمیج،
# و مدیریت نفوذپذیری فلزی (Penetration) همگام با ویژگی‌های سلاح.
# -------------------------------------------------------------

var speed: float = 300.0
var damage: float = 15.0
var direction: Vector2 = Vector2.ZERO
var penetration_count: int = 1
var life_time: float = 4.0

func _ready() -> void:
	# اتصال پاسخ شوک به برخوردها
	connect("body_entered", Callable(self, "_on_body_entered"))
	
	# نابودی تضمینی پس از اتمام عمر پرتابه
	var timer = get_tree().create_timer(life_time)
	timer.connect("timeout", Callable(self, "queue_free"))

func _physics_process(delta: float) -> void:
	if direction != Vector2.ZERO:
		global_position += direction * speed * delta

func _on_body_entered(body: Node2D) -> void:
	if body.is_in_group("enemies") and body.has_method("take_damage"):
		body.take_damage(damage)
		penetration_count -= 1
		
		# اگر ظرفیت نفوذ گلوله به پایان رسید، حذف می‌شود
		if penetration_count <= 0:
			queue_free()

func _draw() -> void:
	# ترسیم یک دایره روشن آبی برای نمایش گلوله جادویی
	draw_circle(Vector2.ZERO, 6.0, Color(0.2, 0.6, 1.0, 0.9))
	draw_circle(Vector2.ZERO, 3.0, Color.WHITE) # هسته نورانی گلوله
