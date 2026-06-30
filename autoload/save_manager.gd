extends Node

# -------------------------------------------------------------
# SaveManager - مدیریت ذخیره‌سازی داده‌ها به صورت کاملاً آفلاین
# نقش: ذخیره‌سازی بالاترین امتیاز بازی (High Score)، سکه‌های کسب شده
# و تجهیزات یا لول‌های باز شده با استفاده از سیستم ذخیره‌سازی محلی Godot.
# -------------------------------------------------------------

const SAVE_PATH = "user://offline_save.dat"

var high_score: int = 0
var total_gold: int = 0
var unlocked_weapons: Array = ["pistol"]
var max_completed_stage: int = 0 # آخرین مرحله تکمیل شده با موفقیت

func _ready() -> void:
	load_game()

func save_game() -> void:
	var file = FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file:
		var save_data = {
			"high_score": high_score,
			"total_gold": total_gold,
			"unlocked_weapons": unlocked_weapons,
			"max_completed_stage": max_completed_stage
		}
		file.store_var(save_data)
		file.close()

func load_game() -> void:
	if FileAccess.file_exists(SAVE_PATH):
		var file = FileAccess.open(SAVE_PATH, FileAccess.READ)
		if file:
			var save_data = file.get_var()
			if save_data is Dictionary:
				high_score = save_data.get("high_score", 0)
				total_gold = save_data.get("total_gold", 0)
				unlocked_weapons = save_data.get("unlocked_weapons", ["pistol"])
				max_completed_stage = save_data.get("max_completed_stage", 0)
			file.close()

func save_high_score(new_score: int) -> void:
	if new_score > high_score:
		high_score = new_score
		save_game()
