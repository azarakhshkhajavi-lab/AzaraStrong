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

data class Move(val name:String,val detail:String,val cue:String,val kind:String)
data class Session(val title:String,val focus:String,val moves:List<Move>)
private val sessions=listOf(
 Session("Posture + Back","Open the chest · strengthen the upper back",listOf(
  Move("March + arm sweep","2 min","Stand tall; ribs stacked over hips.","Warm-up"),Move("Band pull-apart","2 × 12","Palms up; draw shoulder blades down and back.","Posture"),Move("One-arm dumbbell row","2 × 10 / side","Support one hand; pull elbow toward back pocket.","Back"),Move("Wall slides","2 × 8","Keep chin gently tucked; move without shrugging.","Posture"),Move("Standing Pallof press","2 × 10 / side","Brace gently and resist turning.","Core"),Move("Suitcase march","2 × 30 sec / side","Hold one weight; stay tall and level.","Core"))),
 Session("Core + Glutes","Build deep core and shape hips without crunches",listOf(
  Move("Side steps with band","2 min","Soft knees; keep toes forward.","Warm-up"),Move("Goblet squat to chair","2 × 10","Sit back under control; drive through whole foot.","Glutes"),Move("Standing band kickback","2 × 12 / side","Small range; squeeze without arching.","Glutes"),Move("Bird dog","2 × 8 / side","Long spine; keep hips square. Skip after meals.","Core"),Move("Side plank from knees","2 × 20 sec / side","Use Pallof press instead if reflux starts.","Core"),Move("Fast low-impact march","3 × 40 sec","Move briskly; breathe steadily.","Conditioning"))),
 Session("Upper Body + Burn","Shoulders, arms and a short low-impact finish",listOf(
  Move("Step touch + reach","2 min","Easy pace; make the reach long.","Warm-up"),Move("Incline push-up","2 × 8–10","Use a wall or counter; move as one line.","Chest"),Move("Dumbbell overhead press","2 × 8","Use light weights; stop if you shrug or arch.","Shoulders"),Move("Band face pull","2 × 12","Pull toward eyebrow level; neck relaxed.","Posture"),Move("Hammer curl + press-out","2 × 10","Keep wrists neutral and ribs quiet.","Arms"),Move("40/20 power circuit","4 rounds","40 sec march or step-jack, 20 sec easy.","Conditioning")))
)
private val Ink=Color(0xFF18333A);private val Teal=Color(0xFF0E6E70);private val Paper=Color(0xFFF7F3EC);private val Coral=Color(0xFFE4745B);private val Mint=Color(0xFFDCEEE8)

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{AzaraTheme{WorkoutApp()}}}}
@Composable
fun AzaraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Teal,
            secondary = Coral,
            background = Paper,
            surface = Color.White,
            onBackground = Ink,
            onSurface = Ink
        ),
        content = content
    )
}

@Composable fun WorkoutApp(){
 val context=androidx.compose.ui.platform.LocalContext.current
 val prefs=remember{context.getSharedPreferences("progress",Context.MODE_PRIVATE)}
 var day by remember{mutableIntStateOf(0)};var seconds by remember{mutableIntStateOf(1200)};var running by remember{mutableStateOf(false)};var streak by remember{mutableIntStateOf(prefs.getInt("streak",0))}
 val completed=remember{mutableStateMapOf<String,Boolean>().apply{sessions.indices.forEach{d->sessions[d].moves.indices.forEach{i->val k=d.toString()+"-"+i;if(prefs.getBoolean(k,false))put(k,true)}}}}
 LaunchedEffect(running,seconds){if(running&&seconds>0){delay(1000);seconds--}else if(seconds==0)running=false}
 val session=sessions[day]
 Scaffold(bottomBar={NavigationBar(containerColor=Color.White){NavigationBarItem(true,{},icon={Icon(Icons.Default.PlayArrow,null)},label={Text("Workout")});NavigationBarItem(false,{},icon={Icon(Icons.Default.Info,null)},label={Text("Guide")})}}){pad->
  Column(Modifier.fillMaxSize().background(Paper).padding(pad).verticalScroll(rememberScrollState())){
   Box(Modifier.fillMaxWidth().background(Ink).padding(24.dp)){Column{Text("AZARA STRONG",color=Color(0xFFF2B69D),fontWeight=FontWeight.Bold,letterSpacing=2.sp);Spacer(Modifier.height(12.dp));Text("Stand taller.\nFeel stronger.",color=Color.White,fontSize=38.sp,lineHeight=42.sp,fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(12.dp));Text("Your 3-day home plan · 20 minutes · weights + bands",color=Color(0xFFD8E2E1),fontSize=14.sp)}}
   Column(Modifier.padding(16.dp)){
    Card(colors=CardDefaults.cardColors(containerColor=Mint),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(18.dp)){Text("Your goal, honestly",color=Teal,fontWeight=FontWeight.Bold,fontSize=19.sp);Spacer(Modifier.height(6.dp));Text("Core training makes your middle stronger, but belly fat changes through consistent full-body movement, nutrition, sleep and time.",lineHeight=21.sp)}}
    Spacer(Modifier.height(24.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text("THIS WEEK",color=Coral,fontWeight=FontWeight.Bold,fontSize=12.sp);Text("Your sessions",fontSize=28.sp,fontWeight=FontWeight.Bold)};Surface(shape=CircleShape,color=Color.White){Text("Streak  "+streak,Modifier.padding(horizontal=14.dp,vertical=9.dp),fontWeight=FontWeight.Bold)}}
    Spacer(Modifier.height(14.dp));ScrollableTabRow(day,containerColor=Color.Transparent,edgePadding=0.dp,indicator={}){sessions.forEachIndexed{i,s->FilterChip(day==i,{day=i;seconds=1200;running=false},label={Text("Day "+(i+1)+" · "+s.title)},modifier=Modifier.padding(end=8.dp))}}
    Spacer(Modifier.height(12.dp));Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(18.dp)){
     Text(session.focus,color=Color.Gray,fontSize=13.sp);Text(session.title,fontSize=27.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp))
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("%02d:%02d".format(seconds/60,seconds%60),fontSize=34.sp,fontWeight=FontWeight.Black);Button(onClick={if(seconds==0)seconds=1200 else running=!running}){Text(if(seconds==0)"Reset" else if(running)"Pause" else "Start timer")}}
     Spacer(Modifier.height(10.dp));LinearProgressIndicator(progress={session.moves.indices.count{i->completed[day.toString()+"-"+i]==true}.toFloat()/session.moves.size},modifier=Modifier.fillMaxWidth(),color=Teal);Spacer(Modifier.height(14.dp))
     session.moves.forEachIndexed{i,m->val k=day.toString()+"-"+i;MoveRow(m,i,completed[k]==true){val v=completed[k]!=true;completed[k]=v;prefs.edit().putBoolean(k,v).apply()}}
     Button(onClick={session.moves.indices.forEach{i->val k=day.toString()+"-"+i;completed[k]=true;prefs.edit().putBoolean(k,true).apply()}},modifier=Modifier.fillMaxWidth().padding(top=10.dp)){Icon(Icons.Default.Check,null);Spacer(Modifier.width(8.dp));Text("Complete session")}
    }}
    Spacer(Modifier.height(22.dp));Text("Movement beyond the mat",fontSize=26.sp,fontWeight=FontWeight.Bold);Text("Add a 10-minute easy walk after lunch and dinner on 5 days each week. Gentle walking is usually more comfortable than vigorous activity after eating.",color=Color.DarkGray,lineHeight=21.sp)
    OutlinedButton(onClick={streak++;prefs.edit().putInt("streak",streak).apply()},modifier=Modifier.fillMaxWidth().padding(vertical=8.dp)){Text("Mark this week complete")}
    GuideCard("For straighter shoulders","Think “collarbones wide,” not shoulders forced back. Rows, pull-aparts and face pulls make upright posture easier.")
    GuideCard("For frequent heartburn","Train before eating or wait about 2–3 hours after a larger meal. Choose standing core work when symptoms are active.")
    GuideCard("How heavy?","The final two repetitions should feel challenging while your form stays clean. Add resistance only when every set feels easy.")
    Text("Educational fitness guidance—not a medical diagnosis. Stop for chest pain, faintness, unusual breathlessness or sharp pain.",Modifier.padding(10.dp),fontSize=12.sp,color=Color.Gray,fontStyle=FontStyle.Italic)
   }
  }
 }
}

@Composable fun MoveRow(move:Move,index:Int,checked:Boolean,onToggle:()->Unit){
 Surface(Modifier.fillMaxWidth().padding(vertical=4.dp).clickable{onToggle()},shape=RoundedCornerShape(12.dp),color=if(checked)Mint else Color(0xFFFBFAF7)){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){
  Surface(shape=CircleShape,color=if(checked)Teal else Color.Transparent,border=if(checked)null else BorderStroke(1.dp,Color.LightGray),modifier=Modifier.size(36.dp)){Box(contentAlignment=Alignment.Center){if(checked)Icon(Icons.Default.Check,null,tint=Color.White)else Text((index+1).toString(),fontWeight=FontWeight.Bold)}}
  Column(Modifier.padding(horizontal=12.dp).weight(1f)){Text(move.kind.uppercase(),color=Coral,fontSize=10.sp,fontWeight=FontWeight.Bold);Text(move.name,fontWeight=FontWeight.Bold);Text(move.cue,color=Color.Gray,fontSize=12.sp,lineHeight=16.sp)};Text(move.detail,fontWeight=FontWeight.Bold,fontSize=12.sp)
 }}
}
@Composable fun GuideCard(title:String,body:String){Card(Modifier.fillMaxWidth().padding(vertical=6.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(16.dp)){Text(title,fontWeight=FontWeight.Bold,fontSize=18.sp);Spacer(Modifier.height(4.dp));Text(body,color=Color.DarkGray,lineHeight=20.sp)}}}
