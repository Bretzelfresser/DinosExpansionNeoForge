// DinosExpansion Wiki Core JavaScript Logic

document.addEventListener("DOMContentLoaded", () => {
  // Global Navigation Highlight
  highlightNavLink();

  // Page Routing Logic
  if (document.getElementById("dino-grid")) {
    initCatalogPage();
  } else if (document.getElementById("dino-detail-container")) {
    initDetailPage();
  } else if (document.getElementById("taming-calc-container")) {
    initCalculatorPage();
  }
});

// Helper: Highlight active nav link
function highlightNavLink() {
  const currentPath = window.location.pathname;
  const navLinks = document.querySelectorAll(".nav-link");
  
  navLinks.forEach(link => {
    const href = link.getAttribute("href");
    if (currentPath.includes(href) && href !== "index.html" && href !== "/") {
      link.classList.add("active");
    } else if ((currentPath.endsWith("/") || currentPath.includes("index.html")) && (href === "index.html" || href === "/")) {
      link.classList.add("active");
    } else {
      link.classList.remove("active");
    }
  });
}

// ----------------------------------------------------
// 1. CATALOG PAGE LOGIC
// ----------------------------------------------------
function initCatalogPage() {
  const dinoGrid = document.getElementById("dino-grid");
  const searchInput = document.getElementById("search-input");
  const dietFilter = document.getElementById("diet-filter");
  const classFilter = document.getElementById("class-filter");

  // Initial render
  renderCatalog(window.DINOSAURS_DATA);

  // Setup event listeners
  searchInput.addEventListener("input", filterDinos);
  dietFilter.addEventListener("change", filterDinos);
  classFilter.addEventListener("change", filterDinos);

  function filterDinos() {
    const query = searchInput.value.toLowerCase().trim();
    const diet = dietFilter.value;
    const classVal = classFilter.value;

    const filtered = window.DINOSAURS_DATA.filter(dino => {
      const matchesSearch = dino.name.toLowerCase().includes(query) || 
                            dino.tagline.toLowerCase().includes(query);
      const matchesDiet = diet === "all" || dino.diet.toLowerCase() === diet.toLowerCase();
      const matchesClass = classVal === "all" || dino.class.toLowerCase() === classVal.toLowerCase();

      return matchesSearch && matchesDiet && matchesClass;
    });

    renderCatalog(filtered);
  }

  function renderCatalog(dinos) {
    dinoGrid.innerHTML = "";

    if (dinos.length === 0) {
      dinoGrid.innerHTML = `
        <div class="hud-panel" style="grid-column: 1 / -1; text-align: center; padding: 3rem;">
          <h3 class="highlight-cyan" style="font-family: var(--font-title); font-size: 1.5rem; margin-bottom: 0.5rem;">TELEMETRY ERROR: NO CREATURES FOUND</h3>
          <p style="color: var(--text-muted);">Adjust search parameters or filters to re-establish neural link.</p>
        </div>
      `;
      return;
    }

    dinos.forEach(dino => {
      const card = document.createElement("a");
      card.className = "dino-card";
      card.href = `dino.html?id=${dino.id}`;
      
      const badgeClass = dino.diet.toLowerCase() === "carnivore" ? "carnivore" : "herbivore";

      card.innerHTML = `
        <div class="dino-image-container">
          <img src="${dino.image}" alt="${dino.name}" class="dino-img" loading="lazy">
          <div class="dino-badge ${badgeClass}">${dino.diet}</div>
        </div>
        <div class="dino-card-content">
          <h3 class="dino-card-title">${dino.name}</h3>
          <div class="dino-card-tagline">${dino.tagline}</div>
          <p class="dino-card-desc">${dino.description}</p>
          <div class="dino-card-meta">
            <div class="meta-item">Type: <span>${dino.tameType}</span></div>
            <div class="meta-item">Class: <span>${dino.class}</span></div>
          </div>
        </div>
      `;

      dinoGrid.appendChild(card);
    });
  }
}

// ----------------------------------------------------
// 2. DETAIL PAGE LOGIC
// ----------------------------------------------------
function initDetailPage() {
  const params = new URLSearchParams(window.location.search);
  const dinoId = params.get("id");
  
  if (!dinoId) {
    window.location.href = "index.html";
    return;
  }

  const dino = window.DINOSAURS_DATA.find(d => d.id === dinoId);
  if (!dino) {
    window.location.href = "index.html";
    return;
  }

  // Populate basic text
  document.getElementById("dino-title").textContent = dino.name;
  document.getElementById("dino-tagline").textContent = dino.tagline;
  document.getElementById("dino-description").textContent = dino.description;
  document.getElementById("dino-portrait").src = dino.image;
  document.getElementById("dino-portrait").alt = dino.name;
  
  // Set Diet Badge
  const dietBadge = document.getElementById("dino-diet-badge");
  dietBadge.textContent = dino.diet;
  dietBadge.className = `dino-badge ${dino.diet.toLowerCase()}`;

  // Populate Base Stats
  const baseStats = dino.baseStats;
  const maxStats = { health: 1200, stamina: 500, oxygen: 700, food: 4000, weight: 600, melee: 100, speed: 150, torpidity: 2000 };

  for (const [stat, val] of Object.entries(baseStats)) {
    const valText = document.getElementById(`stat-val-${stat}`);
    const barFill = document.getElementById(`stat-bar-${stat}`);
    if (valText && barFill) {
      valText.textContent = val;
      // Calculate percentage bar width (capped at 100%)
      const max = maxStats[stat] || val * 1.5;
      const pct = Math.min(100, Math.ceil((val / max) * 100));
      // Trigger width animation after a short delay
      setTimeout(() => {
        barFill.style.width = `${pct}%`;
      }, 100);
    }
  }

  // Populate Dossier Tabs content
  // 1. Preferred Foods List
  const foodContainer = document.getElementById("food-preferences-list");
  foodContainer.innerHTML = "";
  dino.taming.preferredFoods.forEach(food => {
    const row = document.createElement("div");
    row.className = "food-row";
    row.innerHTML = `
      <div class="food-info">
        <span class="food-icon">${food.icon}</span>
        <span>${food.name}</span>
      </div>
      <div class="food-affinity">+${food.affinity} Affinity <span style="color: var(--text-muted); font-size: 0.85rem;">(Food: ${food.foodValue})</span></div>
    `;
    foodContainer.appendChild(row);
  });

  // 2. Taming Items
  const itemsContainer = document.getElementById("taming-items-list");
  itemsContainer.innerHTML = "";
  dino.taming.tamingItems.forEach(item => {
    const li = document.createElement("div");
    li.className = "food-row";
    li.style.background = "rgba(0, 240, 255, 0.02)";
    li.innerHTML = `
      <div class="food-info" style="color: var(--color-cyan);">
        <span>⚙️</span>
        <span>${item.name}</span>
      </div>
      <div class="highlight-cyan" style="font-family: var(--font-hud); font-size: 1.1rem;">Qty: ${item.quantity}</div>
    `;
    itemsContainer.appendChild(li);
  });

  // 3. Taming Strategy
  const strategyContainer = document.getElementById("taming-strategy-steps");
  strategyContainer.innerHTML = "";
  dino.taming.strategy.forEach((step, idx) => {
    const li = document.createElement("li");
    li.className = "strategy-step";
    
    // Parse mini markdown bold formatting **Text**
    const formattedText = step.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");

    li.innerHTML = `
      <div class="step-num">${idx + 1}</div>
      <div class="step-content">${formattedText}</div>
    `;
    strategyContainer.appendChild(li);
  });

  // 4. Utility Roles
  const utilityContainer = document.getElementById("utility-roles-list");
  utilityContainer.innerHTML = "";
  dino.utility.forEach(role => {
    const li = document.createElement("li");
    li.className = "strategy-step";
    
    // Parse mini markdown bold formatting
    const formattedText = role.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");

    li.innerHTML = `
      <div class="step-num" style="background: rgba(0,255,102,0.1); color: var(--color-green); border-color: var(--color-green); box-shadow: 0 0 5px var(--color-green-glow);">✓</div>
      <div class="step-content">${formattedText}</div>
    `;
    utilityContainer.appendChild(li);
  });

  // Setup detail page links
  document.getElementById("calc-link-btn").href = `calculator.html?id=${dino.id}`;

  // Tabs Interactivity
  const tabBtns = document.querySelectorAll(".tab-btn");
  const tabPanes = document.querySelectorAll(".tab-pane");

  tabBtns.forEach(btn => {
    btn.addEventListener("click", () => {
      const targetTab = btn.getAttribute("data-tab");
      
      tabBtns.forEach(b => b.classList.remove("active"));
      tabPanes.forEach(p => p.classList.remove("active"));
      
      btn.classList.add("active");
      document.getElementById(`tab-pane-${targetTab}`).classList.add("active");
    });
  });
}

// ----------------------------------------------------
// 3. TAMING CALCULATOR PAGE LOGIC
// ----------------------------------------------------
function initCalculatorPage() {
  const dinoSelect = document.getElementById("calc-dino-select");
  const levelSlider = document.getElementById("calc-level-slider");
  const levelInput = document.getElementById("calc-level-input");
  const multSelect = document.getElementById("calc-mult-select");
  const calcResultsBody = document.getElementById("calc-results-body");
  
  // Populate Dino Select dropdown
  dinoSelect.innerHTML = "";
  window.DINOSAURS_DATA.forEach(d => {
    const opt = document.createElement("option");
    opt.value = d.id;
    opt.textContent = d.name;
    dinoSelect.appendChild(opt);
  });

  // Check URL parameters for preset dino ID
  const params = new URLSearchParams(window.location.search);
  const presetId = params.get("id");
  if (presetId && window.DINOSAURS_DATA.some(d => d.id === presetId)) {
    dinoSelect.value = presetId;
  }

  // Synchronize Level Slider and Text Input
  levelSlider.addEventListener("input", () => {
    levelInput.value = levelSlider.value;
    calculateTame();
  });

  levelInput.addEventListener("input", () => {
    let val = parseInt(levelInput.value);
    if (isNaN(val)) val = 1;
    if (val < 1) val = 1;
    if (val > 150) val = 150;
    levelSlider.value = val;
    calculateTame();
  });

  dinoSelect.addEventListener("change", calculateTame);
  multSelect.addEventListener("change", calculateTame);

  // Initial Calculation
  calculateTame();

  function calculateTame() {
    const dinoId = dinoSelect.value;
    const level = parseInt(levelSlider.value);
    const multiplier = parseFloat(multSelect.value);

    const dino = window.DINOSAURS_DATA.find(d => d.id === dinoId);
    if (!dino) return;

    // Taming Math Formulas (mimicking ARK mechanics)
    // 1. Taming affinity required scales up with level
    const affinityNeeded = dino.taming.baseAffinity + (level - 1) * dino.taming.affinityPerLevel;
    
    // 2. Torpor scales up with level
    const torporPool = dino.taming.baseTorpor + (level - 1) * dino.taming.torporPerLevel;
    
    // 3. Torpor drain rate scales up with level
    const torporDrainRate = dino.taming.baseTorporDrain + (level - 1) * dino.taming.torporDrainPerLevel;

    // Update UI Stats display panel on the calculator
    document.getElementById("calc-dino-name").textContent = dino.name;
    document.getElementById("calc-stat-health").textContent = dino.baseStats.health + (level - 1) * 35;
    document.getElementById("calc-stat-weight").textContent = dino.baseStats.weight + (level - 1) * 5;
    document.getElementById("calc-stat-torpor").textContent = Math.ceil(torporPool);
    
    // Time the dino will stay asleep from full torpor to zero
    const wakeUpSeconds = torporPool / torporDrainRate;
    document.getElementById("calc-time-wake").textContent = formatTime(wakeUpSeconds);

    // Render food options calculations
    calcResultsBody.innerHTML = "";

    dino.taming.preferredFoods.forEach(food => {
      // Affinity per food scales with server taming rate multiplier
      const foodAffinity = food.affinity * multiplier;
      const quantity = Math.ceil(affinityNeeded / foodAffinity);
      
      // Taming Time calculation based on food consumption interval
      // Food drain per second
      const foodDrainRate = dino.taming.foodDrainRate; 
      // Dinosaur will eat when food drops by the food value of the item
      const timePerFeed = food.foodValue / foodDrainRate;
      const totalTameTimeSeconds = quantity * timePerFeed;

      // Narcotics calculations
      // How much torpor will decay over the taming duration
      const totalTorporDecay = totalTameTimeSeconds * torporDrainRate;
      // We start at 100% torpor. If decay exceeds the torpor pool, we must feed narcotics to make up the difference
      const torporDeficit = totalTorporDecay - torporPool;
      
      let narcoticsNeeded = 0;
      let narcoberriesNeeded = 0;

      if (torporDeficit > 0) {
        // 1 Narcotic increases torpor by 40
        narcoticsNeeded = Math.ceil(torporDeficit / 40);
        // 1 Narcoberry increases torpor by 7.5
        narcoberriesNeeded = Math.ceil(torporDeficit / 7.5);
      }

      // Taming Effectiveness & Bonus Levels
      // Base effectiveness is roughly 100% minus a decay factor based on food count
      // Kibble maintains high effectiveness, Raw meat decays faster
      let decayFactor = 0.05; // kibble base decay
      if (food.id.includes("kibble")) {
        decayFactor = 0.002;
      } else if (food.id.includes("prime") || food.id.includes("mutton")) {
        decayFactor = 0.015;
      } else if (food.id.includes("berry") || food.id.includes("crop")) {
        decayFactor = 0.025;
      } else {
        decayFactor = 0.04; // standard raw meat/berries
      }

      const effectiveness = Math.max(10, 100 - (quantity * decayFactor));
      const bonusLevels = Math.floor((level * (effectiveness / 100)) / 2);

      const tr = document.createElement("tr");

      // Check if alert warning is needed for this food
      const warningText = totalTameTimeSeconds > wakeUpSeconds 
        ? `<span class="highlight-cyan" style="font-size: 0.8rem; display: block; color: var(--color-orange); text-shadow: none;">⚠️ WAKES UP BEFORE TAME</span>` 
        : ``;

      tr.innerHTML = `
        <td>
          <div style="display:flex; align-items:center; gap:0.5rem;">
            <span>${food.icon}</span>
            <div>
              <span class="highlight-cyan">${food.name}</span>
              ${warningText}
            </div>
          </div>
        </td>
        <td class="highlight-green">${quantity}</td>
        <td>${formatTime(totalTameTimeSeconds)}</td>
        <td>
          <span class="highlight-cyan">${narcoticsNeeded}</span> 
          <span style="color: var(--text-muted); font-size: 0.9rem;">(${narcoberriesNeeded} Berries)</span>
        </td>
        <td>
          <span class="highlight-green">${Math.round(effectiveness)}%</span>
          <span style="color: var(--text-muted); font-size: 0.9rem;">(+${bonusLevels} Lvl)</span>
        </td>
      `;

      calcResultsBody.appendChild(tr);
    });
  }
}

// Helper: Format seconds into readable H:M:S
function formatTime(totalSeconds) {
  if (totalSeconds === Infinity || isNaN(totalSeconds)) return "N/A";
  
  const h = Math.floor(totalSeconds / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = Math.round(totalSeconds % 60);

  let result = "";
  if (h > 0) result += `${h}h `;
  if (m > 0 || h > 0) result += `${m}m `;
  result += `${s}s`;
  
  return result;
}
