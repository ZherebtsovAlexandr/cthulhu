package domain

enum class PerfettoThreadState(val string: String) {
    Running("Running"),
    Runnable("R"),
    RunnablePreempted("R+"),
    Sleeping("S"),
    UninterruptibleSleep("D"),
    Stopped("T"),
    Traced("t"),
    ExitDead("X"),
    ExitZombie("Z"),
    TaskDead("x"),
    Idle("I"),
    WakeKill("K"),
    Waking("W"),
    NoLoad("N"),
    Parked("P");

    companion object {
        fun fromValue(value: String): PerfettoThreadState = when (value) {
            "Running" -> Running
            "R" -> Runnable
            "R+" -> RunnablePreempted
            "S" -> Sleeping
            "D" -> UninterruptibleSleep
            "T" -> Stopped
            "t" -> Traced
            "X" -> ExitDead
            "Z" -> ExitZombie
            "x" -> TaskDead
            "I" -> Idle
            "K" -> WakeKill
            "W" -> Waking
            "N" -> NoLoad
            "P" -> Parked
            else -> throw IllegalArgumentException()
        }
    }
}


//R	Runnable
//R+	Runnable (Preempted)
//S	Sleeping
//D	Uninterruptible Sleep
//T	Stopped
//t	Traced
//X	Exit (Dead)
//Z	Exit (Zombie)
//x	Task Dead
//I	Idle
//K	Wake Kill
//W	Waking
//N	No Load
//P	Parked
