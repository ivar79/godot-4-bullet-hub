extends CanvasLayer

@onready var title_label: Label = $Control/Panel/MarginContainer/VBoxContainer/Title
@onready var info_label: Label = $Control/Panel/MarginContainer/VBoxContainer/InfoLabel
@onready var next_stage_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/NextStageBtn

func _ready() -> void:
	get_tree().paused = true
	
	title_label.text = "🎉 مرحله " + str(GameManager.current_stage) + " با موفقیت انجام شد! 🎉"
	
	var minutes = int(GameManager.time_elapsed) / 60
	var seconds = int(GameManager.time_elapsed) % 60
	var time_str = "%02d:%02d" % [minutes, seconds]
	
	info_label.text = (
		"آمار این مرحله:\n\n" +
		"🔹 مرحله کامل شده: " + str(GameManager.current_stage) + "\n" +
		"🔹 دشمنان نابود شده: " + str(GameManager.stage_kills) + "\n" +
		"🔹 زمان بقاء: " + time_str + "\n" +
		"🔹 امتیاز کل: " + str(GameManager.current_score) + "\n\n" +
		"قهرمان شما کاملاً مداوا شد و آماده ورود به چالش مرحله بعدی است!"
	)
	
	next_stage_btn.pressed.connect(_on_next_stage_pressed)

func _on_next_stage_pressed() -> void:
	GameManager.start_next_stage()
	queue_free()
