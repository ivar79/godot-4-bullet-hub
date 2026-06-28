extends CanvasLayer

# -------------------------------------------------------------
# GameOver - صفحه پایان بازی و باخت بازیکن
# نقش: نمایش جزئیات نهایی رکورد بقاء شامل زمان زنده ماندن و امتیاز
# نهایی، ذخیره نتایج توسط SaveManager، و دکمه راه‌اندازی مجدد بازی (Restart).
# -------------------------------------------------------------

@onready var score_label: Label = $Control/Panel/MarginContainer/VBoxContainer/ScoreLabel
@onready var time_label: Label = $Control/Panel/MarginContainer/VBoxContainer/TimeLabel
@onready var restart_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/RestartButton

func _ready() -> void:
	# نمایش نتایج بر اساس وضعیت مدیریت زنده بازی
	score_label.text = "امتیاز نهایی کسب شده: " + str(GameManager.current_score)
	
	var minutes = int(GameManager.time_elapsed) / 60
	var seconds = int(GameManager.time_elapsed) % 60
	time_label.text = "مدت زمان بقاء: %02d:%02d" % [minutes, seconds]
	
	restart_btn.pressed.connect(_on_restart_pressed)

func _on_restart_pressed() -> void:
	get_tree().paused = false
	get_tree().change_scene_to_file("res://scenes/levels/main_level.tscn")
