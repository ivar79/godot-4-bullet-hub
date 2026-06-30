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

func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed:
			touch_start_position = event.position
			is_dragging = true
		else:
			is_dragging = false
			joystick_released.emit()
			
	elif event is InputEventScreenDrag and is_dragging:
		var current_pos = event.position
		var delta = current_pos - touch_start_position
		
		if delta.length() > drag_threshold:
			var movement_direction = delta.normalized()
			joystick_moved.emit(movement_direction)
			
	# شبیه‌سازی موس/ماوس‌پد لپ‌تاپ به عنوان لمس برای دسکتاپ و امولاتور
	elif event is InputEventMouseButton:
		if event.button_index == MOUSE_BUTTON_LEFT:
			if event.pressed:
				touch_start_position = event.position
				is_dragging = true
			else:
				is_dragging = false
				joystick_released.emit()
				
	elif event is InputEventMouseMotion and is_dragging:
		var current_pos = event.position
		var delta = current_pos - touch_start_position
		
		if delta.length() > drag_threshold:
			var movement_direction = delta.normalized()
			joystick_moved.emit(movement_direction)
