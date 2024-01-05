package dev.liquid.osamaclient.feature.implementation.helper

class Timer(private val time: Int = 0) {
  private val endTime = System.currentTimeMillis() + time
  fun hasEnded() = System.currentTimeMillis() >= endTime
}