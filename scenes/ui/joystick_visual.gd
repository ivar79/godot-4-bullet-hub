extends Node2D

const BASE_RADIUS = 80.0
const KNOB_RADIUS = 32.0

# موقعیت ثابت و ارگونومیک جوی‌استیک در پایین سمت چپ صفحه نمایش (مختصات بومی ۷۲۰ در ۱۲۸۰)
var joystick_center: Vector2 = Vector2(160, 1100)
var joystick_active: bool = false
var joystick_dir: Vector2 = Vector2.ZERO

func _ready() -> void:
	EventBus.joystick_updated.connect(_on_joystick_updated)

func _on_joystick_updated(active: bool, start_pos: Vector2, direction: Vector2) -> void:
	joystick_active = active
	joystick_dir = direction
	queue_redraw()

func _draw() -> void:
	# افکت آلفا برای تغییر شفافیت جذاب زمان لمس یا عدم لمس
	var base_alpha = 0.45 if joystick_active else 0.18
	var knob_alpha = 0.75 if joystick_active else 0.30
	
	# رسم دایره بیرونی جوی‌استیک (پس‌زمینه نیمه‌شفاف و خط دور دایره)
	draw_circle(joystick_center, BASE_RADIUS, Color(1, 1, 1, base_alpha * 0.3))
	draw_arc(joystick_center, BASE_RADIUS, 0, TAU, 48, Color(1, 1, 1, base_alpha), 3.0)
	
	# محاسبه موقعیت دستگیره وسط (Knob)
	var handle_pos = joystick_center + joystick_dir * 38.0
	draw_circle(handle_pos, KNOB_RADIUS, Color(1, 1, 1, knob_alpha))
	draw_arc(handle_pos, KNOB_RADIUS, 0, TAU, 24, Color(1, 1, 1, knob_alpha * 1.2), 2.5)
