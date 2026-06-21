extends Node2D

# -------------------------------------------------------------
# JoystickVisual - رسم برداری جوی‌استیک لمسی بر روی موبایل
# نقش: گوش دادن به رویداد لمس صفحه از طریق EventBus و ترسیم
# دایره‌های آیکون گرافیکی جوی‌استیک در زمان فعال بودن روی تاچ‌اسکرین.
# -------------------------------------------------------------

var joystick_active: bool = false
var joystick_start: Vector2 = Vector2.ZERO
var joystick_dir: Vector2 = Vector2.ZERO

func _ready() -> void:
	# اتصال مستقیم بدون واسطه به رویداد تغییر وضعیت جوی‌استیک از EventBus
	EventBus.connect("joystick_updated", Callable(self, "_on_joystick_updated"))

func _on_joystick_updated(active: bool, start_pos: Vector2, direction: Vector2) -> void:
	joystick_active = active
	joystick_start = start_pos
	joystick_dir = direction
	queue_redraw() # فراخوانی رسم مجدد بوم CanvasItem جهت ترسیم دایره‌ها

func _draw() -> void:
	if joystick_active:
		# ترسیم دایره شفاف بیرونی جوی‌استیک روی تاچ هاب موبایل
		draw_circle(joystick_start, 60.0, Color(1, 1, 1, 0.15))
		draw_circle(joystick_start, 60.0, Color(1, 1, 1, 0.3), false, 2.0)
		
		# ترسیم دسته داخلی متحرک متناسب با جهت هدایت
		var handle_pos = joystick_start + joystick_dir * 35.0
		draw_circle(handle_pos, 22.0, Color(0.2, 0.6, 1.0, 0.5))
		draw_circle(handle_pos, 22.0, Color.WHITE, false, 1.5)
