package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WebsiteSection
import com.example.model.WebsiteSectionType

@Composable
fun WebsiteBuilderView(
    onExportToCode: (html: String, css: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = remember {
        mutableStateListOf(
            WebsiteSection(
                type = WebsiteSectionType.NAVBAR,
                title = "NovaFlow",
                subtitle = "Home, Features, Pricing, Contact"
            ),
            WebsiteSection(
                type = WebsiteSectionType.HERO,
                title = "Build Fast on Mobile",
                subtitle = "The premier developer environment for web and canvas games directly in your pocket.",
                buttonText = "Get Started Free"
            ),
            WebsiteSection(
                type = WebsiteSectionType.FEATURES,
                title = "Engineered for Performance",
                subtitle = "Offline-first development • Full Canvas 2D engine • Integrated AI assistant"
            ),
            WebsiteSection(
                type = WebsiteSectionType.FOOTER,
                title = "© 2026 NovaFlow. Built with HTML Live."
            )
        )
    }

    var showAddSectionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ViewQuilt, null, tint = Color(0xFF38BDF8))
                    Spacer(Modifier.width(8.dp))
                    Text("Visual Website Builder", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text("Assemble blocks and export real, clean HTML & CSS", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }

            Button(
                onClick = {
                    val (html, css) = compileSectionsToCode(sections)
                    onExportToCode(html, css)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF0F172A)),
                modifier = Modifier.testTag("export_website_code_btn")
            ) {
                Icon(Icons.Default.Code, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Export Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Section Blocks List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(sections, key = { _, s -> s.id }) { index, section ->
                SectionCard(
                    section = section,
                    canMoveUp = index > 0,
                    canMoveDown = index < sections.size - 1,
                    onMoveUp = {
                        val item = sections.removeAt(index)
                        sections.add(index - 1, item)
                    },
                    onMoveDown = {
                        val item = sections.removeAt(index)
                        sections.add(index + 1, item)
                    },
                    onDelete = {
                        sections.removeAt(index)
                    },
                    onUpdate = { updated ->
                        sections[index] = updated
                    }
                )
            }

            item {
                OutlinedButton(
                    onClick = { showAddSectionDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("add_section_block_btn")
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add Section Block", color = Color(0xFF38BDF8))
                }
            }
        }
    }

    if (showAddSectionDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddSectionDialog = false },
            title = { Text("Choose Section Type") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WebsiteSectionType.values().forEach { type ->
                        Surface(
                            onClick = {
                                sections.add(
                                    WebsiteSection(
                                        type = type,
                                        title = when (type) {
                                            WebsiteSectionType.NAVBAR -> "Company"
                                            WebsiteSectionType.HERO -> "Catchy Headline"
                                            WebsiteSectionType.FEATURES -> "Core Features"
                                            WebsiteSectionType.TEXT_IMAGE -> "About Our Mission"
                                            WebsiteSectionType.STATS -> "10k+ Downloads"
                                            WebsiteSectionType.GALLERY -> "Product Showcase"
                                            WebsiteSectionType.CTA -> "Ready to Transform Your Workflow?"
                                            WebsiteSectionType.CONTACT -> "Get in Touch"
                                            WebsiteSectionType.FOOTER -> "© 2026 Brand. All rights reserved."
                                        },
                                        subtitle = type.description,
                                        buttonText = if (type == WebsiteSectionType.HERO || type == WebsiteSectionType.CTA) "Explore" else ""
                                    )
                                )
                                showAddSectionDialog = false
                            },
                            color = Color(0xFF1E293B),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(type.displayName, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                Text(type.description, fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddSectionDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionCard(
    section: WebsiteSection,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (WebsiteSection) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(section.type.displayName, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 14.sp)
                Row {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowUpward, null, tint = if (canMoveUp) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowDownward, null, tint = if (canMoveDown) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = section.title,
                onValueChange = { onUpdate(section.copy(title = it)) },
                label = { Text("Headline / Title", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = section.subtitle,
                onValueChange = { onUpdate(section.copy(subtitle = it)) },
                label = { Text("Description / Items", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth()
            )

            if (section.buttonText.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = section.buttonText,
                    onValueChange = { onUpdate(section.copy(buttonText = it)) },
                    label = { Text("Button Text", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun compileSectionsToCode(sections: List<WebsiteSection>): Pair<String, String> {
    val htmlBuilder = StringBuilder()
    htmlBuilder.append("""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Created with HTML Live</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
""")

    for (s in sections) {
        when (s.type) {
            WebsiteSectionType.NAVBAR -> {
                val links = s.subtitle.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                htmlBuilder.append("""  <nav class="navbar">
    <div class="logo">${s.title}</div>
    <ul class="nav-links">
${links.joinToString("\n") { "      <li><a href='#'>$it</a></li>" }}
    </ul>
  </nav>
""")
            }
            WebsiteSectionType.HERO -> {
                htmlBuilder.append("""  <header class="hero">
    <h1>${s.title}</h1>
    <p>${s.subtitle}</p>
    ${if (s.buttonText.isNotBlank()) "<button class=\"cta-btn\">${s.buttonText}</button>" else ""}
  </header>
""")
            }
            WebsiteSectionType.FEATURES -> {
                val items = s.subtitle.split("•", ",").map { it.trim() }.filter { it.isNotEmpty() }
                htmlBuilder.append("""  <section class="section features">
    <h2>${s.title}</h2>
    <div class="cards-grid">
${items.joinToString("\n") { "      <div class=\"feature-card\"><h3>✦</h3><p>$it</p></div>" }}
    </div>
  </section>
""")
            }
            WebsiteSectionType.FOOTER -> {
                htmlBuilder.append("""  <footer class="footer">
    <p>${s.title}</p>
  </footer>
""")
            }
            else -> {
                htmlBuilder.append("""  <section class="section">
    <h2>${s.title}</h2>
    <p>${s.subtitle}</p>
    ${if (s.buttonText.isNotBlank()) "<button class=\"cta-btn\">${s.buttonText}</button>" else ""}
  </section>
""")
            }
        }
    }

    htmlBuilder.append("""  <script src="script.js"></script>
</body>
</html>""")

    val css = """* { box-sizing: border-box; margin: 0; padding: 0; }
body {
  font-family: system-ui, -apple-system, sans-serif;
  background: #0f172a;
  color: #f8fafc;
  line-height: 1.6;
}
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: #1e293b;
}
.logo { font-size: 1.2rem; font-weight: bold; color: #38bdf8; }
.nav-links { display: flex; list-style: none; gap: 1rem; }
.nav-links a { color: #cbd5e1; text-decoration: none; }
.hero {
  text-align: center;
  padding: 4rem 1.5rem;
  background: radial-gradient(circle at center, #1e293b, #0f172a);
}
.hero h1 { font-size: 2.2rem; color: #38bdf8; margin-bottom: 1rem; }
.hero p { color: #94a3b8; max-width: 600px; margin: 0 auto 1.5rem; }
.cta-btn {
  background: #38bdf8;
  color: #0f172a;
  border: none;
  padding: 12px 24px;
  font-size: 1rem;
  font-weight: bold;
  border-radius: 8px;
  cursor: pointer;
}
.section { padding: 3rem 1.5rem; max-width: 900px; margin: 0 auto; text-align: center; }
.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
  margin-top: 1.5rem;
}
.feature-card {
  background: #1e293b;
  padding: 1.5rem;
  border-radius: 10px;
  border: 1px solid #334155;
}
.feature-card h3 { color: #38bdf8; margin-bottom: 0.5rem; }
.footer {
  text-align: center;
  padding: 2rem 1rem;
  border-top: 1px solid #1e293b;
  color: #64748b;
  font-size: 0.9rem;
}"""

    return Pair(htmlBuilder.toString(), css)
}
