class_name MobileInput
extends Node

# -------------------------------------------------------------
# MobileInput - تحلیل ورودی لمسی روی موبایل
# نقش: پیاده‌سازی جوی‌استیک لمسی مجازی یا ژست‌های سوایپ
# برای جابجایی راحت قهرمان بازی با یک انگشت (Single-finger controller).
# -------------------------------------------------------------

var touch_start_position: Vector2 = Vector2.ZERO
var is_dragging: bool = false
var drag_threshold: float = 10.0

signal joystick_moved(direction: Vector2)
signal joystick_released

func _unhandled_input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed:
			touch_start_position = event.position
			is_dragging = true
		else:
			is_dragging = false
			emit_signal("joystick_released")
			
	elif event is InputEventScreenDrag and is_dragging:
		var current_pos = event.position
		var delta = current_pos - touch_start_position
		
		if delta.length() > drag_threshold:
			var movement_direction = delta.normalized()
			emit_signal("joystick_moved", movement_direction)
