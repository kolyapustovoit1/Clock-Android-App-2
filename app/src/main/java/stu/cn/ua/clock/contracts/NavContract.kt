package stu.cn.ua.clock.contracts

interface NavContract {
    fun back()

    fun toClockScreen()

    fun toTimerScreen()

    fun toSettingsScreen()

    fun toAboutScreen()

    fun updateScreenTitle(title: String)
}