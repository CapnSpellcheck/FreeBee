package com.letstwinkle.freebee.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letstwinkle.freebee.bodyStyle

@Composable
fun RulesSheet() {
   val headerStyle = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
   val ruleModifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
   
   Column(Modifier.fillMaxWidth().padding(16.dp)) {
      Text("Rules", fontWeight = FontWeight.Bold, fontSize = 34.sp)
      Spacer(Modifier.height(32.dp))
      
      Text(
         "Create words using letters from the hive",
         Modifier.padding(vertical = 3.dp),
         style = headerStyle
      )
      Divider()
      Text("Words must contain at least 4 letters.", ruleModifier, style = bodyStyle)
      Text("Words must include the center letter.", ruleModifier, style = bodyStyle)
      Text("Our word list does not include words that are obscure, hyphenated, or proper nouns.", ruleModifier, style = bodyStyle)
      Text("No cussing either, sorry.", ruleModifier, style = bodyStyle)
      Text("Letters can be used more than once.", ruleModifier, style = bodyStyle)
      
      Spacer(Modifier.height(22.dp))
      
      Text("How to score", Modifier.padding(vertical = 3.dp), style = headerStyle)
      Divider()
      Text("4-letter words are worth 1 point each.", ruleModifier, style = bodyStyle)
      Text("Longer words earn 1 point per letter.", ruleModifier, style = bodyStyle)
      Text("Each puzzle includes at least one “pangram” which uses every letter. These are worth 7 extra points!", ruleModifier, style = bodyStyle)
   }
}
