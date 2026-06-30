extends CanvasLayer

@onready var option1_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/Option1
@onready var option2_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/Option2
@onready var option3_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/Option3

func _ready() -> void:
	get_tree().paused = true
	var player = get_tree().get_first_node_in_group("player")
	if player == null:
		_resume()
		return
		
	var pool = []
	
	# ۱. افزودن ارتقاهای آماری مجاز بر اساس سطح فعلی آن‌ها در کاراکتر (تا سقف ۷ سطح)
	for type in ["speed", "damage", "fire_rate", "armor", "magnet", "proj_speed"]:
		var current_lvl = player.upgrade_levels.get(type, 0)
		if current_lvl < 7:
			pool.append(_get_upgrade_details(type, current_lvl))
			
	# ۲. افزودن ارتقاء تراز برای هر یک از سلاح‌های مجهز شده بازیکن (تا سقف ۷ سطح)
	if "equipped_weapons" in player and "weapon_levels" in player:
		for weapon in player.equipped_weapons:
			var w_id = weapon.weapon_id
			var current_lvl = player.weapon_levels.get(w_id, 1)
			if current_lvl < 7:
				pool.append(_get_weapon_upgrade_details(w_id, current_lvl))
			
	# ۳. افزودن ارتقاء ترمیم (همیشه در دسترس به عنوان گزینه نجات)
	pool.append({
		"title": "درمان فوری جان",
		"type": "health",
		"value": 0.50,
		"description": "بازگرداندن ۵۰٪ از نوار سلامتی بازیکن",
		"level": 0
	})
	
	# ۴. افزودن شانس کسب سلاح جدید (در صورت خالی بودن ظرفیت)
	if player.equipped_weapons.size() < 6:
		pool.append({
			"title": "دریافت سلاح جدید",
			"type": "weapon",
			"value": 0.0,
			"description": "اضافه کردن یک سلاح خودکار جدید به زرادخانه خود",
			"level": 0
		})
		
	pool.shuffle()
	
	# انتخاب ۳ گزینه نهایی
	var chosen = pool.slice(0, 3)
	
	# نمایش در دکمه‌ها (در صورتی که گزینه‌های کمتری موجود بود، دکمه‌های اضافی مخفی می‌شوند)
	if chosen.size() >= 1:
		_setup_button(option1_btn, chosen[0])
	else:
		option1_btn.visible = false
		
	if chosen.size() >= 2:
		_setup_button(option2_btn, chosen[1])
	else:
		option2_btn.visible = false
		
	if chosen.size() >= 3:
		_setup_button(option3_btn, chosen[2])
	else:
		option3_btn.visible = false

func _setup_button(btn: Button, upgrade: Dictionary) -> void:
	if upgrade["type"] == "health":
		btn.text = "❤️ " + upgrade["title"] + " ❤️\n" + upgrade["description"]
	elif upgrade["type"] == "weapon":
		btn.text = "⚔️ " + upgrade["title"] + " ⚔️\n" + upgrade["description"]
	else:
		btn.text = upgrade["title"] + "\n" + upgrade["description"]
	btn.pressed.connect(func(): _apply_upgrade(upgrade))

func _get_upgrade_details(type: String, current_lvl: int) -> Dictionary:
	var next_lvl = current_lvl + 1
	var title = ""
	var desc = ""
	var val = 0.0
	
	match type:
		"speed":
			val = 1.20
			if next_lvl <= 5:
				title = "سرعت حرکت (سطح " + str(next_lvl) + "/7)"
				desc = "افزایش سرعت دویدن قهرمان به میزان ۲۰٪"
			elif next_lvl == 6:
				title = "⚡ سرعت حرکت (سطح میثیک ۶/۷) ⚡"
				desc = "افزایش سرعت دویدن قهرمان به میزان ۲۵٪"
			elif next_lvl == 7:
				title = "👑 سرعت افسانه‌ای: شبح باد (۷/۷) 👑"
				desc = "سرعت +۵۰٪ و انتشار خودکار هاله صدمه‌زننده فانتوم دوره‌ای هر ۲ ثانیه!"
				val = 1.50
		"damage":
			val = 1.30
			if next_lvl <= 5:
				title = "قدرت ضربه (سطح " + str(next_lvl) + "/7)"
				desc = "افزایش قدرت تخریب و دمیج سلاح‌ها به میزان ۳۰٪"
			elif next_lvl == 6:
				title = "🔥 قدرت ضربه (سطح میثیک ۶/۷) 🔥"
				desc = "افزایش قدرت تخریب و دمیج سلاح‌ها به میزان ۳۵٪"
			elif next_lvl == 7:
				title = "👑 دمیج افسانه‌ای: غرش ارباب (۷/۷) 👑"
				desc = "دمیج دو برابر (+۱۰۰٪)، عبور آسان گلوله‌ها از دشمنان و انفجار سراسری تمام گلوله‌ها بر روی هدف!"
				val = 2.0
		"fire_rate":
			val = 1.25
			if next_lvl <= 5:
				title = "سرعت شلیک (سطح " + str(next_lvl) + "/7)"
				desc = "افزایش سرعت بارگذاری و تیراندازی سلاح‌ها به میزان ۲۵٪"
			elif next_lvl == 6:
				title = "☄️ سرعت شلیک (سطح میثیک ۶/۷) ☄️"
				desc = "افزایش سرعت بارگذاری و تیراندازی سلاح‌ها به میزان ۳۰٪"
			elif next_lvl == 7:
				title = "👑 شلیک افسانه‌ای: رگبار بی‌نهایت (۷/۷) 👑"
				desc = "سرعت شلیک +۷۵٪ و پرتاب شگفت‌انگیز ۳ گلوله همزمان به جای ۱ گلوله!"
				val = 1.75
		"armor":
			val = 5.0
			if next_lvl <= 5:
				title = "زره دفاعی (سطح " + str(next_lvl) + "/7)"
				desc = "کاهش آسیب دریافتی از زامبی‌ها به میزان ۵ واحد"
			elif next_lvl == 6:
				title = "🛡️ زره دفاعی (سطح میثیک ۶/۷) 🛡️"
				desc = "کاهش آسیب دریافتی از زامبی‌ها به میزان ۸ واحد"
				val = 8.0
			elif next_lvl == 7:
				title = "👑 دفاع افسانه‌ای: سنگر اِجیس (۷/۷) 👑"
				desc = "زره +۲۵ واحد، التیام مستمر ۵٪ جان در هر ضربه و بازتاب صدمه خار (۱۵۰ دمیج) به زامبی‌های دور مچ!"
				val = 25.0
		"magnet":
			val = 1.50
			if next_lvl <= 5:
				title = "آهنربای الماس (سطح " + str(next_lvl) + "/7)"
				desc = "افزایش محدوده جمع‌آوری الماس‌های تجربه به میزان ۵۰٪"
			elif next_lvl == 6:
				title = "🔮 آهنربای الماس (سطح میثیک ۶/۷) 🔮"
				desc = "افزایش محدوده جمع‌آوری الماس‌های تجربه به میزان ۶۰٪"
				val = 1.60
			elif next_lvl == 7:
				title = "👑 مگنت افسانه‌ای: جاذبه کهکشانی (۷/۷) 👑"
				desc = "شعاع مگنت دو برابر و جذب خودکار کل الماس‌های موجود در نقشه به سمت شما هر ۱۲ ثانیه یک‌بار!"
				val = 2.0
		"proj_speed":
			val = 1.20
			if next_lvl <= 5:
				title = "سرعت گلوله‌ها (سطح " + str(next_lvl) + "/7)"
				desc = "افزایش سرعت پرواز شلیک‌ها به میزان ۲۰٪"
			elif next_lvl == 6:
				title = "🌀 سرعت گلوله‌ها (سطح میثیک ۶/۷) 🌀"
				desc = "افزایش سرعت پرواز شلیک‌ها به میزان ۲۵٪"
				val = 1.25
			elif next_lvl == 7:
				title = "👑 سرعت افسانه‌ای: شتاب شفق (۷/۷) 👑"
				desc = "سرعت گلوله‌ها +۶۰٪ و یخ‌زدگی آنی زامبی‌ها (کاهش سرعت حرکت زامبی‌ها به نصف به مدت ۳ ثانیه)!"
				val = 1.60
				
	# ساخت نمایش ستاره‌ها برای سطوح استاندارد
	var stars = ""
	if next_lvl <= 5:
		for i in range(next_lvl):
			stars += "⭐"
		for i in range(5 - next_lvl):
			stars += "☆"
		title += " " + stars
	elif next_lvl == 6:
		title += " ⭐⭐⭐⭐⭐★"
		
	return {"title": title, "type": type, "value": val, "description": desc, "level": next_lvl}

func _get_weapon_upgrade_details(w_id: String, current_lvl: int) -> Dictionary:
	var next_lvl = current_lvl + 1
	var title = ""
	var desc = ""
	
	match w_id:
		"magic_wand":
			if next_lvl <= 5:
				title = "عصای جادویی (سطح " + str(next_lvl) + "/7)"
				desc = "افزایش قدرت تخریب و سرعت گلوله جادویی"
			elif next_lvl == 6:
				title = "⭐ عصای جادویی (سطح میثیک ۶/۷) ⭐"
				desc = "افزایش دمیج و پرتاب یک گلوله کمکی همزمان"
			elif next_lvl == 7:
				title = "👑 عصای جادویی افسانه‌ای: طوفان ارواح (۷/۷) 👑"
				desc = "دمیج فوق‌العاده، پرتاب گلوله‌های جادویی رگباری با اندازه بزرگ‌تر و نفوذ بی‌نهایت!"
		"fireball":
			if next_lvl <= 5:
				title = "گلوله آتشین (سطح " + str(next_lvl) + "/7)"
				desc = "افزایش دمیج و محدوده انفجار آتش"
			elif next_lvl == 6:
				title = "⭐ گلوله آتشین (سطح میثیک ۶/۷) ⭐"
				desc = "انفجار بزرگ‌تر و ایجاد افکت سوزاندن دشمنان"
			elif next_lvl == 7:
				title = "👑 گلوله آتشین افسانه‌ای: ابرنواختر (۷/۷) 👑"
				desc = "هنگام برخورد به ۸ گلوله آتشین کوچک‌تر در تمام جهات منفجر می‌شود!"
		"lightning_chain":
			if next_lvl <= 5:
				title = "زنجیره رعد و برق (سطح " + str(next_lvl) + "/7)"
				desc = "افزایش دمیج و تعداد زنجیره به زامبی‌های بیشتر"
			elif next_lvl == 6:
				title = "⭐ زنجیره رعد و برق (سطح میثیک ۶/۷) ⭐"
				desc = "اتصال به ۲ زامبی اضافی و گیج کردن (Stun) موقت آن‌ها"
			elif next_lvl == 7:
				title = "👑 رعد و برق افسانه‌ای: خشم خدای رعد (۷/۷) 👑"
				desc = "اتصال به ۱۰ دشمن همزمان و بارش صاعقه مرگبار آسمانی بر سر هر یک!"

	var stars = ""
	if next_lvl <= 5:
		for i in range(next_lvl):
			stars += "⭐"
		for i in range(5 - next_lvl):
			stars += "☆"
		title += " " + stars
	elif next_lvl == 6:
		title += " ⭐⭐⭐⭐⭐★"

	return {
		"title": title,
		"type": "weapon_level",
		"weapon_id": w_id,
		"level": next_lvl,
		"description": desc
	}

func _apply_upgrade(upgrade: Dictionary) -> void:
	var player = get_tree().get_first_node_in_group("player")
	if player == null:
		_resume()
		return
		
	var type = upgrade["type"]
	var level = upgrade["level"]
	
	# ثبت سطح ارتقاء در بازیکن
	if type in player.upgrade_levels:
		player.upgrade_levels[type] = level
		
	match type:
		"speed":
			player.stats.base_speed *= upgrade["value"]
		"damage":
			player.damage_multiplier *= upgrade["value"]
		"health":
			player.heal(player.stats.max_health * upgrade["value"])
		"fire_rate":
			player.fire_rate_multiplier *= upgrade["value"]
			# بروزرسانی آنی تایمر شلیک تمام سلاح‌های فعال
			var holder = player.get_node_or_null("WeaponHolder")
			var parent_to_search = holder if holder else player
			for child in parent_to_search.get_children():
				if child.has_method("update_fire_rate"):
					child.update_fire_rate()
		"armor":
			player.stats.defense += upgrade["value"]
		"magnet":
			player.magnet_multiplier *= upgrade["value"]
			# بروزرسانی آنی تمام الماس‌های موجود روی نقشه
			for gem in get_tree().get_nodes_in_group("gems"):
				if is_instance_valid(gem):
					gem.magnet_radius *= upgrade["value"]
		"proj_speed":
			player.projectile_speed_multiplier *= upgrade["value"]
		"weapon_level":
			var w_id = upgrade["weapon_id"]
			var lvl = upgrade["level"]
			player.weapon_levels[w_id] = lvl
			# اجرای اعلامیه شیک ارتقاء سلاح
			EventBus.show_announcement.emit("⚔️ سلاح به سطح " + str(lvl) + " ارتقاء یافت! ⚔️", Color(0.2, 0.9, 0.6))
		"weapon":
			var new_weapon = WeaponData.new()
			var weapon_pool = [
				{"id": "fireball", "name": "گلوله آتشین ویرانگر", "damage": 25.0, "rate": 0.8, "speed": 280.0, "pen": 1},
				{"id": "lightning_chain", "name": "زنجیره رعد و برق تسلا", "damage": 12.0, "rate": 1.5, "speed": 400.0, "pen": 3}
			]
			var chosen_w = weapon_pool.pick_random()
			new_weapon.weapon_id = chosen_w["id"]
			new_weapon.weapon_name = chosen_w["name"]
			new_weapon.base_damage = chosen_w["damage"]
			new_weapon.fire_rate = chosen_w["rate"]
			new_weapon.projectile_speed = chosen_w["speed"]
			new_weapon.penetration_count = chosen_w["pen"]
			player.equip_weapon(new_weapon)
			
	_resume()

func _resume() -> void:
	get_tree().paused = false
	queue_free()
