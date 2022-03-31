package com.boarbeard.generator.beimax

// TODO, can put inside EventList class once moved to Kotlin
/**
Attempts to fit an event in the eventlist, or will throw exception if goes beyond attempts allowed
 */
fun EventList.fit(attempts: Int = 100000, runner: (currentAttempt: Int) -> Boolean): Boolean {
    var done: Boolean
    var currentAttempts = 0
    do {
        currentAttempts++
        done = runner.invoke(currentAttempts)
    } while (!done && currentAttempts < attempts)

    return done
}