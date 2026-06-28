class_name UpgradeData
extends Resource
# UpgradeData - تعریف یک آپگرید لول‌آپ (پسیو، سلاح، یا ترمیم)

@export var upgrade_id: String = ""
@export var upgrade_name: String = ""
@export var icon_color: Color = Color.WHITE
@export var description: String = ""
@export var type: String = "stat" # stat, weapon, heal, special
@export var max_stacks: int = 5 # حداکثر تعداد دفعات انتخاب این آپگرید
@export var stat_to_modify: String = "" # base_speed, max_health, defense, etc.
@export var stat_multiplier: float = 1.0 # ضریب افزایش (مثلاً 1.15 یعنی +15%)
@export var stat_flat_bonus: float = 0.0 # مقدار ثابت اضافه
@export var weapon_to_add: String = "" # ID سلاحی که اضافه می‌شود
