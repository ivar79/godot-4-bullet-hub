extends CanvasLayer

# -------------------------------------------------------------
# LevelUpPanel - واسط پیشفرض ارتقا تراز و ارتقای متوقف کننده بازی
# نقش: ایجاد یک وقفه استراتژیک در هنگام عبور از تراز قبلی (Level Up)،
# نمایش و انتخاب ۳ گزینه ارتقای تصادفی ملموس، اعمال بوف به بازیکن،
# و فعال‌سازی مجدد جریان بازی پس از کلیک.
# -------------------------------------------------------------

@onready var option1_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/Option1
@onready var option2_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/Option2
@onready var option3_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/Option3

var player: Player = null

func _ready() -> void:
	# متوقف کردن کل موتور بازی برای پاز تداخلی
	get_tree().paused = true
	
	# اتصال به هاب بازیکن در صحنه جاری
	var players = get_tree().get_nodes_in_group("player")
	if players.size() > 0:
		player = players[0] as Player
		
	# تنظیم متن‌ها و استایل دکمه‌های ارتقا به زبان فارسی
	option1_btn.text = "⚡ افزایش سرعت جابجایی (+۱۵٪)"
	option2_btn.text = "💥 تقویت قدرت ضرباتی شلیک (+۲۵٪)"
	option3_btn.text = "❤️ بازیابی کامل میزان سلامتی (Heal)"
	
	option1_btn.connect("pressed", Callable(self, "_on_option_1_pressed"))
	option2_btn.connect("pressed", Callable(self, "_on_option_2_pressed"))
	option3_btn.connect("pressed", Callable(self, "_on_option_3_pressed"))

func _on_option_1_pressed() -> void:
	if player and is_instance_valid(player):
		player.stats.base_speed = player.stats.base_speed * 1.15
	_resume()

func _on_option_2_pressed() -> void:
	if player and is_instance_valid(player):
		# تقویت قدرت تمامی اسلحه های مجهز شده بازیکن
		for weapon in player.equipped_weapons:
			weapon.base_damage = weapon.base_damage * 1.25
	_resume()

func _on_option_3_pressed() -> void:
	if player and is_instance_valid(player):
		# بازیابی کامل سلامتی قهرمان
		player.heal(player.stats.max_health)
	_resume()

func _resume() -> void:
	# لغو وقفه بازی و بستن پنل لول‌آپ
	get_tree().paused = false
	queue_free()
