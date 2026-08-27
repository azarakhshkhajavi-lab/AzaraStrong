package com.azarastrong.app
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Move(val name:String,val detail:String,val cue:String,val kind:String)
data class Session(val title:String,val focus:String,val moves:List<Move>)
private val homeSessions=listOf(
 Session("Posture + Back","Open the chest · strengthen the upper back",listOf(
  Move("March + arm sweep","2 min","Stand tall; ribs stacked over hips.","Warm-up"),Move("Band pull-apart","2 × 12","Palms up; draw shoulder blades down and back.","Posture"),Move("One-arm dumbbell row","2 × 10 / side","Support one hand; pull elbow toward back pocket.","Back"),Move("Wall slides","2 × 8","Keep chin gently tucked; move without shrugging.","Posture"),Move("Standing Pallof press","2 × 10 / side","Brace gently and resist turning.","Core"),Move("Suitcase march","2 × 30 sec / side","Hold one weight; stay tall and level.","Core"))),
 Session("Core + Glutes","Build deep core and shape hips without crunches",listOf(
  Move("Side steps with band","2 min","Soft knees; keep toes forward.","Warm-up"),Move("Goblet squat to chair","2 × 10","Sit back under control; drive through whole foot.","Glutes"),Move("Standing band kickback","2 × 12 / side","Small range; squeeze without arching.","Glutes"),Move("Bird dog","2 × 8 / side","Long spine; keep hips square. Skip after meals.","Core"),Move("Side plank from knees","2 × 20 sec / side","Use Pallof press instead if reflux starts.","Core"),Move("Fast low-impact march","3 × 40 sec","Move briskly; breathe steadily.","Conditioning"))),
 Session("Upper Body + Burn","Shoulders, arms and a short low-impact finish",listOf(
  Move("Step touch + reach","2 min","Easy pace; make the reach long.","Warm-up"),Move("Incline push-up","2 × 8–10","Use a wall or counter; move as one line.","Chest"),Move("Dumbbell overhead press","2 × 8","Use light weights; stop if you shrug or arch.","Shoulders"),Move("Band face pull","2 × 12","Pull toward eyebrow level; neck relaxed.","Posture"),Move("Hammer curl + press-out","2 × 10","Keep wrists neutral and ribs quiet.","Arms"),Move("40/20 power circuit","4 rounds","40 sec march or step-jack, 20 sec easy.","Conditioning"))),
 Session("Full Body Shape","Glutes, waist support and upright posture",listOf(
  Move("March with band pull","2 min","Stand tall and keep your steps light.","Warm-up"),Move("Romanian deadlift","2 × 10","Push hips back; keep weights close to legs.","Glutes"),Move("Split squat to chair","2 × 8 / side","Use the chair for balance and stay upright.","Glutes"),Move("Dumbbell floor press","2 × 10","Keep ribs down. Replace with wall push-up after meals.","Chest"),Move("Standing wood chop","2 × 10 / side","Turn through the upper back without forcing the waist.","Core"),Move("Farmer carry march","2 × 40 sec","Hold weights at your sides and walk tall.","Posture")))
)
private val gymSessions=listOf(
 Session("Gym Back + Posture","Cables and machines for a stronger, more upright upper body",listOf(
  Move("Treadmill warm-up","2 min","Walk tall with shoulders relaxed.","Warm-up"),Move("Lat pulldown","2 × 10","Pull elbows toward your ribs without leaning back.","Back"),Move("Seated cable row","2 × 10","Finish with shoulder blades gently together.","Back"),Move("Reverse pec deck","2 × 12","Use a light weight and keep your neck relaxed.","Posture"),Move("Cable Pallof press","2 × 10 / side","Keep hips and shoulders facing forward.","Core"),Move("Farmer carry","2 × 40 sec","Walk tall with weights beside your legs.","Posture"))),
 Session("Gym Glutes + Core","Build hips and glutes while supporting the waist",listOf(
  Move("Incline treadmill walk","2 min","Use an easy incline and steady breathing.","Warm-up"),Move("Leg press","2 × 10","Place feet slightly high and press through heels.","Glutes"),Move("Hip thrust machine","2 × 10","Finish by squeezing glutes without arching.","Glutes"),Move("Cable glute kickback","2 × 12 / side","Keep the movement controlled and pelvis level.","Glutes"),Move("Abductor machine","2 × 12","Open knees slowly without bouncing.","Glutes"),Move("Cable wood chop","2 × 10 / side","Rotate through the upper body with control.","Core"))),
 Session("Gym Chest + Shoulders","Strengthen the upper body and support better posture",listOf(
  Move("Elliptical warm-up","2 min","Keep your torso tall and movement smooth.","Warm-up"),Move("Chest press machine","2 × 10","Keep shoulder blades supported by the pad.","Chest"),Move("Shoulder press machine","2 × 8","Use a comfortable grip and do not shrug.","Shoulders"),Move("Cable face pull","2 × 12","Pull toward eyebrow height with elbows wide.","Posture"),Move("Cable triceps pressdown","2 × 10","Keep elbows close to your sides.","Arms"),Move("Low-impact bike intervals","4 × 40 sec","Work for 40 seconds, then recover gently.","Conditioning"))),
 Session("Gym Full Body","A balanced strength session for shape and conditioning",listOf(
  Move("Treadmill warm-up","2 min","Walk tall and let your arms swing.","Warm-up"),Move("Smith machine squat","2 × 10","Use a bench target and keep feet firmly planted.","Glutes"),Move("Dumbbell Romanian deadlift","2 × 10","Push hips back and keep weights close.","Glutes"),Move("Assisted pull-up machine","2 × 8","Drive elbows down and keep chest lifted.","Back"),Move("Cable chest press","2 × 10","Brace your middle and press straight forward.","Chest"),Move("Sled push or incline walk","2 × 40 sec","Stay controlled and stop before form changes.","Conditioning")))
)

val Ink=Color(0xFF123F46)
val Teal=Color(0xFF087F83)
val Paper=Color(0xFFFAF7EF)
val Coral=Color(0xFF99752C)
val Mint=Color(0xFFDDEFEA)

@Composable fun TermehBorder(){
 Canvas(Modifier.fillMaxWidth().height(26.dp)){
  val spacing=32.dp.toPx()
  for(i in 0..(size.width/spacing).toInt()){
   val x=i*spacing;val y=size.height/2
   val p=androidx.compose.ui.graphics.Path().apply{
    moveTo(x,y+8.dp.toPx())
    cubicTo(x-12.dp.toPx(),y,x-2.dp.toPx(),y-13.dp.toPx(),x+9.dp.toPx(),y-9.dp.toPx())
    cubicTo(x+1.dp.toPx(),y-8.dp.toPx(),x+13.dp.toPx(),y+8.dp.toPx(),x,y+8.dp.toPx())
    close()
   }
   drawPath(p,Teal.copy(alpha=0.28f),style=androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
   drawCircle(Coral.copy(alpha=0.5f),1.3.dp.toPx(),androidx.compose.ui.geometry.Offset(x,y))
  }
 }
}

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  setContent{AzaraTheme{WorkoutApp()}}
 }
}
@Composable fun AzaraTheme(content:@Composable ()->Unit){
 MaterialTheme(colorScheme=lightColorScheme(primary=Teal,secondary=Coral,background=Paper,surface=Color.White,onBackground=Ink,onSurface=Ink),content=content)
}
@Composable fun WorkoutApp(){
 val context=androidx.compose.ui.platform.LocalContext.current
 val prefs=remember{context.getSharedPreferences("progress",Context.MODE_PRIVATE)}
 var mode by remember{mutableStateOf(prefs.getString("mode","home")?:"home")}
 var selectedDay by remember{mutableStateOf<Int?>(null)}
 var guided by remember{mutableStateOf(false)}
 val sessions=if(mode=="home")homeSessions else gymSessions
 val dates=remember{mutableStateMapOf<String,String>().apply{
  listOf("home","gym").forEach{m->(0..3).forEach{d->prefs.getString("date-"+m+"-"+d,null)?.let{put(m+"-"+d,it)}}}
 }}
 val completed=remember{mutableStateMapOf<String,Boolean>().apply{
  listOf("home","gym").forEach{m->(0..3).forEach{d->(0..5).forEach{i->val k=m+"-"+d+"-"+i;if(prefs.getBoolean(k,false))put(k,true)}}}
 }}
 if(guided&&selectedDay!=null){
  val d=selectedDay!!
  GuidedWorkoutScreen(sessions[d],onExit={guided=false},onMoveComplete={i->
   val k=mode+"-"+d+"-"+i;completed[k]=true;prefs.edit().putBoolean(k,true).apply()
  })
  return
 }
 androidx.activity.compose.BackHandler(selectedDay!=null){selectedDay=null}
 val d=selectedDay
 Scaffold(containerColor=Paper,bottomBar={
  if(d!=null)Surface(color=Paper,shadowElevation=8.dp){
   Button(onClick={
    val k=mode+"-"+d
    if(dates[k]==null){
     val stamp=SimpleDateFormat("MMM d, yyyy",Locale.getDefault()).format(Date())
     dates[k]=stamp;prefs.edit().putString("date-"+k,stamp).apply()
    }
    guided=true
   },modifier=Modifier.navigationBarsPadding().padding(horizontal=24.dp,vertical=14.dp).fillMaxWidth().height(58.dp),shape=RoundedCornerShape(20.dp)){
    Icon(Icons.Default.PlayArrow,null);Spacer(Modifier.width(10.dp));Text("Start workout",fontSize=18.sp,fontWeight=FontWeight.Bold)
   }
  }
 }){padding->
  Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(top=20.dp,bottom=24.dp)){
   if(d==null){
    Row(verticalAlignment=Alignment.CenterVertically){
     Surface(shape=RoundedCornerShape(20.dp),color=Ink,modifier=Modifier.size(54.dp)){
      Image(androidx.compose.ui.res.painterResource(R.drawable.brand_woman),"Azara",Modifier.padding(3.dp))
     }
     Column(Modifier.padding(start=12.dp)){
      Text("AZARA",fontSize=20.sp,letterSpacing=3.sp,fontWeight=FontWeight.Bold)
      Text("Strength with care",fontSize=12.sp,color=Teal)
     }
    }
    Spacer(Modifier.height(16.dp))
    TermehBorder()
    Spacer(Modifier.height(16.dp))
    Text("Your time.\nYour strength.",fontSize=38.sp,lineHeight=43.sp,fontWeight=FontWeight.Bold,color=Ink)
    Spacer(Modifier.height(10.dp))
    Text("Choose your space. Choose your day.",color=Teal,fontSize=15.sp)
    Spacer(Modifier.height(22.dp))
    Row(Modifier.fillMaxWidth().background(Mint,RoundedCornerShape(18.dp)).padding(5.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){
     listOf("home" to "Home","gym" to "Gym").forEach{(value,label)->
      Surface(onClick={mode=value;prefs.edit().putString("mode",value).apply()},color=if(mode==value)Ink else Color.Transparent,shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){
       Text(label,Modifier.padding(14.dp),color=if(mode==value)Color.White else Ink,fontWeight=FontWeight.Bold,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
      }
     }
    }
    Spacer(Modifier.height(26.dp))
    Text("YOUR FOUR DAYS",fontSize=11.sp,letterSpacing=2.sp,color=Teal,fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    sessions.forEachIndexed{i,s->
     val done=s.moves.indices.count{completed[mode+"-"+i+"-"+it]==true}
     Surface(onClick={selectedDay=i},color=if(i==0)Ink else Color.White,shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth().padding(bottom=12.dp)){
      Row(Modifier.padding(20.dp),verticalAlignment=Alignment.CenterVertically){
       Column(Modifier.width(66.dp)){
        Text("DAY",fontSize=11.sp,letterSpacing=1.sp,color=if(i==0)Mint else Teal,fontWeight=FontWeight.Bold)
        Text((i+1).toString().padStart(2,'0'),fontSize=42.sp,fontWeight=FontWeight.Bold,color=if(i==0)Color.White else Ink)
       }
       Column(Modifier.weight(1f).padding(horizontal=10.dp)){
        Text(s.title.removePrefix("Gym "),fontSize=20.sp,lineHeight=24.sp,fontWeight=FontWeight.Bold,color=if(i==0)Color.White else Ink)
        Spacer(Modifier.height(7.dp))
        Text(s.moves.size.toString()+" exercises · "+done+" completed",fontSize=12.sp,color=if(i==0)Mint else Teal)
        dates[mode+"-"+i]?.let{Text("First started "+it,fontSize=11.sp,color=if(i==0)Mint else Teal)}
       }
       Icon(Icons.Default.ChevronRight,"Open day "+(i+1),tint=Coral)
      }
     }
    }
    Spacer(Modifier.height(12.dp))
    Text("Small steps. Stronger every week.",fontSize=15.sp,fontWeight=FontWeight.Medium)
    Spacer(Modifier.height(8.dp))
    Text("For heartburn, avoid exercise soon after a large meal. Stop if a movement causes pain.",fontSize=12.sp,lineHeight=18.sp,color=Teal)
   }else{
    TextButton(onClick={selectedDay=null},contentPadding=PaddingValues(0.dp)){
     Icon(Icons.Default.ArrowBack,null);Spacer(Modifier.width(8.dp));Text("All days")
    }
    Spacer(Modifier.height(18.dp))
    Text(mode.uppercase()+" · DAY "+(d+1),fontSize=12.sp,letterSpacing=2.sp,color=Teal,fontWeight=FontWeight.Bold)
    Text(sessions[d].title.removePrefix("Gym "),fontSize=34.sp,lineHeight=39.sp,fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
    Text(sessions[d].focus,color=Teal,lineHeight=22.sp)
    Spacer(Modifier.height(22.dp))
    Surface(color=Mint,shape=RoundedCornerShape(20.dp)){
     Text("Press Start once. Voice instructions, sets, side changes and the next exercise follow automatically.",Modifier.padding(18.dp),color=Ink,lineHeight=21.sp)
    }
    Spacer(Modifier.height(24.dp))
    sessions[d].moves.forEachIndexed{i,m->
     Row(Modifier.fillMaxWidth().padding(vertical=14.dp),verticalAlignment=Alignment.CenterVertically){
      Text((i+1).toString().padStart(2,'0'),color=Coral,fontSize=23.sp,fontWeight=FontWeight.Bold,modifier=Modifier.width(46.dp))
      Column(Modifier.weight(1f)){
       Text(m.name,fontSize=17.sp,fontWeight=FontWeight.SemiBold)
       Text(m.detail+" · "+m.kind,color=Teal,fontSize=12.sp,modifier=Modifier.padding(top=4.dp))
      }
      if(completed[mode+"-"+d+"-"+i]==true)Icon(Icons.Default.Check,"Previously completed",tint=Teal)
     }
     HorizontalDivider(color=Mint)
    }
   }
  }
 }
}
