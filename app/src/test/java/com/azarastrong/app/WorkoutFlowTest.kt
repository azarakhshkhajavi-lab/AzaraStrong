package com.azarastrong.app
import org.junit.Assert.*
import org.junit.Test
class WorkoutFlowTest {
 @Test fun timedWarmup(){assertEquals(ExerciseDose(1,120,true,1),doseFor("2 min"))}
 @Test fun repetitions(){assertEquals(ExerciseDose(2,12,false,1),doseFor("2 × 12"))}
 @Test fun unilateralRepetitions(){assertEquals(ExerciseDose(2,10,false,2),doseFor("2 × 10 / side"))}
 @Test fun unilateralTimer(){assertEquals(ExerciseDose(2,30,true,2),doseFor("2 × 30 sec / side"))}
 @Test fun startEndCountdown(){assertNull(endingCue(4));assertEquals("3",endingCue(3));assertEquals("2",endingCue(2));assertEquals("1",endingCue(1));assertNull(endingCue(0))}
 @Test fun setsAndSidesStayInOrder(){
  val s=Session("Day","",listOf(Move("Row","2 × 10 / side","","Back"),Move("Wall","2 × 8","","Posture")))
  val steps=workoutSteps(s)
  assertEquals(6,steps.size)
  assertEquals(listOf(1,2,1,2),steps.take(4).map{it.side})
  assertEquals(listOf(1,1,2,2),steps.take(4).map{it.set})
  assertEquals(listOf(0,0,0,0,1,1),steps.map{it.moveIndex})
 }
}
