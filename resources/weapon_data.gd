class_name WeaponData
extends Resource

# -------------------------------------------------------------
# WeaponData - منبع داده‌های سفارشی سلاح (Custom Resource)
# نقش: تعریف ساختار داده‌محور برای سلاح‌های خودکار (Auto-Weapons).
# در بازی‌های Survivor، سلاح‌ها پارامترهای تراز و ویژگی‌های خاص خود را دارند.
# با این ریسورس می‌توان جزییات آماری انواع سلاح‌ها را تعریف نمود.
# -------------------------------------------------------------

@export var weapon_id: String = "magic_wand"
@export var weapon_name: String = "Magic Wand"
@export_multiline var description: String = "Fires magical projectiles at the nearest enemy."
@export var base_damage: float = 15.0
@export var projectile_speed: float = 300.0
@export var fire_rate: float = 1.2 # شلیک در ثانیه
@export var projectile_count: int = 1
@export var penetration_count: int = 1
@export var area_of_effect: float = 1.0 # ضریب اندازه شلیک
@export var icon: Texture2D
@export var weapon_scene: PackedScene # لینک به صحنه گرافیکی سلاح
