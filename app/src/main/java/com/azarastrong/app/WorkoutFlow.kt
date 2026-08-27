package com.azarastrong.app

data class ExerciseDose(val sets:Int,val target:Int,val timed:Boolean,val sides:Int)
data class WorkoutStep(val moveIndex:Int,val set:Int,val side:Int,val dose:ExerciseDose)
fun doseFor(detail:String):ExerciseDose{
 val sets=Regex("""^\s*(\d+)\s*[×x]""").find(detail)?.groupValues?.get(1)?.toInt()?:1
 val timed=detail.contains("sec")||detail.contains("min")
 val after=detail.substringAfter("×",detail).substringAfter("x",detail.substringAfter("×",detail)).trim()
 val number=Regex("""\d+""").find(after)?.value?.toIntOrNull()?:10
 val target=if(detail.contains("min"))number*60 else number
 return ExerciseDose(sets.coerceAtLeast(1),target.coerceAtLeast(1),timed,if(detail.contains("/ side"))2 else 1)
}
fun workoutSteps(session:Session):List<WorkoutStep> = session.moves.flatMapIndexed{i,m->
 val dose=doseFor(m.detail)
 (1..dose.sets).flatMap{set->(1..dose.sides).map{side->WorkoutStep(i,set,side,dose)}}
}
fun endingCue(remaining:Int):String?=if(remaining in 1..3)remaining.toString() else null
