extends CanvasLayer

@onready var hp_bar: ProgressBar = $Control/HPBar
@onready var xp_bar: ProgressBar = $Control/XPBar
@onready var score_label: Label = $Control/ScoreLabel
@onready var level_label: Label = $Control/LevelLabel
@onready var timer_label: Label = $Control/TimerLabel
@onready var pause_btn: Button = $Control/PauseBtn
@onready var pause_overlay: ColorRect = $Control/PauseOverlay
@onready var pause_panel: Panel = $Control/PausePanel
@onready var resume_btn: Button = $Control/PausePanel/VBoxContainer/ResumeBtn
@onready var menu_btn: Button = $Control/PausePanel/VBoxContainer/MenuBtn

var announcement_label: Label

func _ready() -> void:
	EventBus.player_health_changed.connect(_on_player_health_changed)
	EventBus.xp_collected.connect(_on_xp_collected)
	EventBus.player_level_up.connect(_on_player_level_up)
	EventBus.show_announcement.connect(_on_show_announcement)
	
	score_label.text = "Score: 0"
	level_label.text = "Lvl: 1"
	
	pause_btn.pressed.connect(_on_pause_pressed)
	resume_btn.pressed.connect(_on_resume_pressed)
	menu_btn.pressed.connect(_on_menu_pressed)
	
	_setup_announcement_label()

func _setup_announcement_label() -> void:
	announcement_label = Label.new()
	announcement_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	announcement_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	announcement_label.autowrap_mode = TextServer.AUTOWRAP_WORD
	announcement_label.set_anchors_and_offsets_preset(Control.PRESET_CENTER)
	announcement_label.grow_horizontal = Control.GROW_DIRECTION_BOTH
	announcement_label.grow_vertical = Control.GROW_DIRECTION_BOTH
	announcement_label.custom_minimum_size = Vector2(320, 100)
	# موقعیت دهی درست زیر تایمر و لول
	announcement_label.position.y = 125.0
	announcement_label.add_theme_font_size_override("font_size", 22)
	announcement_label.visible = false
	$Control.add_child(announcement_label)

func _on_show_announcement(text: String, color: Color) -> void:
	if announcement_label:
		announcement_label.text = text
		announcement_label.add_theme_color_override("font_color", color)
		announcement_label.visible = true
		announcement_label.modulate.a = 0.0
		announcement_label.scale = Vector2(0.8, 0.8)
		announcement_label.pivot_offset = announcement_label.size / 2.0
		
		var tween = create_tween().set_parallel(true)
		tween.tween_property(announcement_label, "modulate:a", 1.0, 0.25)
		tween.tween_property(announcement_label, "scale", Vector2(1.15, 1.15), 0.25)
		
		await get_tree().create_timer(3.5).timeout
		
		if is_instance_valid(announcement_label):
			var fade_out = create_tween().set_parallel(true)
			fade_out.tween_property(announcement_label, "modulate:a", 0.0, 0.3)
			fade_out.tween_property(announcement_label, "scale", Vector2(0.8, 0.8), 0.3)
			await fade_out.finished
			if is_instance_valid(announcement_label):
				announcement_label.visible = false

func _process(_delta: float) -> void:
	if GameManager.is_game_active:
		timer_label.text = format_time(GameManager.time_elapsed) + " / " + format_time(GameManager.get_stage_duration()) + " | Stage: " + str(GameManager.current_stage)
		score_label.text = "Score: " + str(GameManager.current_score)

func format_time(time_in_seconds: float) -> String:
	var minutes = int(time_in_seconds) / 60
	var seconds = int(time_in_seconds) % 60
	return "%02d:%02d" % [minutes, seconds]

func _on_player_health_changed(current: float, max_val: float) -> void:
	hp_bar.max_value = max_val
	hp_bar.value = current

func _on_xp_collected(_amt: int, current: int, next_lvl_xp: int) -> void:
	xp_bar.max_value = next_lvl_xp
	xp_bar.value = current

func _on_player_level_up(new_level: int) -> void:
	level_label.text = "Lvl: " + str(new_level)

func _on_pause_pressed() -> void:
	pause_overlay.visible = true
	pause_panel.visible = true
	GameManager.pause_game(true)

func _on_resume_pressed() -> void:
	pause_overlay.visible = false
	pause_panel.visible = false
	GameManager.pause_game(false)

func _on_menu_pressed() -> void:
	get_tree().paused = false
	get_tree().change_scene_to_file("res://scenes/ui/main_menu.tscn")
