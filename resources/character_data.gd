class_name CharacterData
extends Resource

# -------------------------------------------------------------
# CharacterData - منبع داده‌های سفارشی بازیکن (Custom Resource)
# نقش: تعریف مشخصات اولیه بازیکن به صورت داده‌محور (Data-Driven).
# طراحان بازی می‌توانند با ایجاد چند نمونه از این منبع، کلاس‌های متفاوتی
# از قهرمانان بسازند (مثلاً نینجا، شوالیه، جادوگر) بدون دستکاری در کد بازیکن.
# -------------------------------------------------------------

@export var character_name: String = "Survivor"
@export var max_health: float = 100.0
@export var base_speed: float = 180.0
@export var defense: float = 5.0
@export var life_steal_pct: float = 0.0 # درصد جذب جان به همراه آسیب
@export var character_sprite: Texture2D
