package com.webiki.bucketlist.enums

enum class GoalCategory(var value: String, var position: Int) {
    Health("Здоровье", 10),
    Career("Карьера", 11),
    Finance("Финансы", 12),
    LifeBrightness("Яркость жизни", 13),
    Relationships("Отношения", 14),
    FamilyAndFriends("Семья и друзья", 15),
    Materialist("Материальное", 16),
    Selfbuilding("Самосовершенствование", 17),
    Other("Другое", 18)
}