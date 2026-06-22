// Dinosaur Database for DinosExpansion Wiki
const DINOSAURS_DATA = [
  {
    id: "trex",
    name: "Tyrannosaurus Rex",
    tagline: "The Apex Predator of the Island",
    diet: "Carnivore",
    tameType: "Knockout",
    class: "Fighter",
    image: "assets/images/trex.png",
    description: "Tyrannosaurus dominum is a massive, aggressive predator that dominates the Island's jungles and mountains. Taming a Rex is a rite of passage for any tribe, transforming them from prey into rulers of the wilderness. They possess high health, terrifying melee damage, and an intimidating roar that can stun smaller creatures.",
    baseStats: {
      health: 1100,
      stamina: 420,
      oxygen: 150,
      food: 3000,
      weight: 500,
      melee: 62,
      speed: 100,
      torpidity: 1550
    },
    taming: {
      baseAffinity: 1800,
      affinityPerLevel: 90,
      baseTorpor: 1550,
      torporPerLevel: 93,
      baseTorporDrain: 1.67, // torpor drain per second
      torporDrainPerLevel: 0.12,
      foodDrainRate: 0.833, // food points lost per second (approx 50 per minute)
      preferredFoods: [
        { id: "exceptional_kibble", name: "Exceptional Kibble", affinity: 400, foodValue: 80, icon: "🥚" },
        { id: "raw_mutton", name: "Raw Mutton", affinity: 200, foodValue: 50, icon: "🥩" },
        { id: "raw_prime_meat", name: "Raw Prime Meat", affinity: 150, foodValue: 50, icon: "🍖" },
        { id: "cooked_prime_meat", name: "Cooked Prime Meat", affinity: 75, foodValue: 50, icon: "🥓" },
        { id: "raw_meat", name: "Raw Meat", affinity: 50, foodValue: 50, icon: "🥩" }
      ],
      tamingItems: [
        { name: "Stone Dinosaur Gateway & Gates (for trap)", quantity: "4 gateways, 2 gates" },
        { name: "Large Bear Trap", quantity: "2-3" },
        { name: "Longneck Rifle with Shocking Tranquilizer Darts", quantity: "20-40 shots" },
        { name: "Narcotics", quantity: "150-300" }
      ],
      strategy: [
        "**Prepare the Trap**: Build a classic 3x3 stone trap with a ramp, or place 4 Stone Dinosaur Gateways in a U-shape. Put a Large Bear Trap in the center.",
        "**Lure the Rex**: Aggro the Rex and lead it into the gateways or trap. Once it gets stuck in the Bear Trap, quickly run behind it and place the 4th gateway to lock it in.",
        "**Tranquilize**: Shoot the Rex in the body or tail with Tranquilizer Darts. Headshots do NOT deal bonus torpor to a Rex, so aim for the largest body mass.",
        "**Starve Tame (Recommended)**: Keep the Rex asleep using Narcotics, but do not put taming food in its inventory until its food stat has dropped enough to consume all the food at once. This protects against losing taming effectiveness if the Rex gets attacked while unconscious."
      ],
      utility: [
        "**Boss Fighter**: The cornerstone of most boss arena teams due to their high health pool and raw damage output.",
        "**Alpha Slayer**: Easily hunts down Alpha Raptors, Alpha Carnos, and Alpha Rexes for massive XP and high-tier loot.",
        "**Intimidation**: Their roar causes wild and enemy dinosaurs below level 50 to defecate and stun-lock momentarily."
      ]
    }
  },
  {
    id: "raptor",
    name: "Velociraptor",
    tagline: "The Pack Predator of the Undergrowth",
    diet: "Carnivore",
    tameType: "Knockout",
    class: "Fighter",
    image: "assets/images/raptor.png",
    description: "Velociraptor is a fast, agile pack hunter notorious for pouncing on unsuspecting survivors. Found in almost every biome except mountains and deep seas, they attack in packs, buffing each other's melee damage and resistance. Once tamed, they make swift scouts and deadly light cavalry.",
    baseStats: {
      health: 200,
      stamina: 150,
      oxygen: 150,
      food: 1200,
      weight: 140,
      melee: 15,
      speed: 100,
      torpidity: 180
    },
    taming: {
      baseAffinity: 600,
      affinityPerLevel: 30,
      baseTorpor: 180,
      torporPerLevel: 10.8,
      baseTorporDrain: 0.45,
      torporDrainPerLevel: 0.03,
      foodDrainRate: 0.416, // food points lost per second
      preferredFoods: [
        { id: "simple_kibble", name: "Simple Kibble", affinity: 150, foodValue: 50, icon: "🥚" },
        { id: "raw_mutton", name: "Raw Mutton", affinity: 100, foodValue: 50, icon: "🥩" },
        { id: "raw_prime_meat", name: "Raw Prime Meat", affinity: 75, foodValue: 50, icon: "🍖" },
        { id: "raw_meat", name: "Raw Meat", affinity: 25, foodValue: 50, icon: "🥩" }
      ],
      tamingItems: [
        { name: "Bola", quantity: "2-3" },
        { name: "Crossbow with Tranquilizer Arrows", quantity: "5-15 shots" },
        { name: "Wooden Club (Alternative for low levels)", quantity: "1" },
        { name: "Narcotics / Narcoberries", quantity: "20-50" }
      ],
      strategy: [
        "**Immobilize**: Raptors are very fast and can pounce to dismount you. Always approach with a Bola wound up. Throw it as they run towards you to trap them for 30 seconds.",
        "**Knock Out**: While the Raptor is bola'd, shoot it in the head with tranquilizer arrows. Raptors have a 2.5x headshot torpor multiplier, so aim carefully.",
        "**Protect the Tame**: Raptors have relatively low health, so be careful not to hit them after they fall unconscious. Clear out local predators like Dilophosaurs before starting."
      ],
      utility: [
        "**Scout**: Extremely fast runners and capable of jumping over obstacles, making them excellent early-game scouts.",
        "**Pack Leader**: Taming multiple Raptors gives them a Pack Buff (+45% damage, +25% damage resistance) when grouped together. The highest level Raptor becomes the Alpha, gaining a pack roar.",
        "**Trapper**: Their pack strike can pin down humans and small creatures, allowing easy kills."
      ]
    }
  },
  {
    id: "trike",
    name: "Triceratops",
    tagline: "The Shielded Shield of the Plains",
    diet: "Herbivore",
    tameType: "Knockout",
    class: "Harvester",
    image: "assets/images/trike.png",
    description: "Triceratops styrax is a sturdy, armored herbivore that travels in small herds. Their thick skull crest is highly resistant to damage and torpor, acting as a natural shield. Tamed Trikes are the ultimate early-game companion, offering excellent berry gathering, knockback combat capability, and load-bearing capacity.",
    baseStats: {
      health: 375,
      stamina: 150,
      oxygen: 150,
      food: 3000,
      weight: 365,
      melee: 32,
      speed: 100,
      torpidity: 250
    },
    taming: {
      baseAffinity: 900,
      affinityPerLevel: 45,
      baseTorpor: 250,
      torporPerLevel: 15,
      baseTorporDrain: 0.35,
      torporDrainPerLevel: 0.02,
      foodDrainRate: 0.300,
      preferredFoods: [
        { id: "simple_kibble", name: "Simple Kibble", affinity: 225, foodValue: 80, icon: "🥚" },
        { id: "mejoberry", name: "Mejoberry", affinity: 30, foodValue: 30, icon: "🍇" },
        { id: "crops", name: "Savoroot/Longrass/Citronal/Rockarrot", affinity: 40, foodValue: 40, icon: "🌽" },
        { id: "other_berries", name: "Other Berries", affinity: 20, foodValue: 20, icon: "🍒" }
      ],
      tamingItems: [
        { name: "Wooden Billboard or Stone Pillars (for trap)", quantity: "3-4" },
        { name: "Slingshot or Bow with Tranq Arrows", quantity: "15-30 shots" },
        { name: "Mejoberries", quantity: "150-400" },
        { name: "Narcotics", quantity: "40-100" }
      ],
      strategy: [
        "**Avoid the Head**: Never shoot a Trike in the head shield! The skull crest blocks 85% of incoming damage and torpor. Always circle around and shoot them in the flank, rump, or legs.",
        "**Kiting**: Since Trikes are slow but charge forward when angered, use a cliff or build a basic structure of three billboards in a V-shape. Stand inside and shoot the Trike as it struggles to turn.",
        "**Keep Distance**: Avoid their charge attack, which inflicts a stun/slow effect on players."
      ],
      utility: [
        "**Berry Harvester**: Their wide sweep attack gathers hundreds of Narcoberries, Mejoberries, and Fiber in seconds.",
        "**Early Guard**: Their high knockback on basic attacks can keep Carnos and Raptors completely out of range, allowing them to fight off threats twice their size.",
        "**Charge Attack**: Can build up speed to ram targets, dealing massive damage and stunning them."
      ]
    }
  },
  {
    id: "pteranodon",
    name: "Pteranodon",
    tagline: "The Swift Skyrunner of the Coast",
    diet: "Carnivore",
    tameType: "Knockout",
    class: "Flyer",
    image: "assets/images/pteranodon.png",
    description: "Pteranodon longiceps is a skittish, fish-eating flyer that nests along beaches. They immediately take flight and escape at the slightest sign of danger, making them tricky to catch. Taming a Pteranodon is a milestone achievement, unlocking full aerial travel, scouting, and transportation.",
    baseStats: {
      health: 210,
      stamina: 150,
      oxygen: 150,
      food: 1200,
      weight: 120,
      melee: 18,
      speed: 100,
      torpidity: 120
    },
    taming: {
      baseAffinity: 600,
      affinityPerLevel: 30,
      baseTorpor: 120,
      torporPerLevel: 7.2,
      baseTorporDrain: 0.53,
      torporDrainPerLevel: 0.04,
      foodDrainRate: 0.500,
      preferredFoods: [
        { id: "simple_kibble", name: "Simple Kibble", affinity: 150, foodValue: 50, icon: "🥚" },
        { id: "raw_mutton", name: "Raw Mutton", affinity: 120, foodValue: 50, icon: "🥩" },
        { id: "raw_prime_meat", name: "Raw Prime Meat", affinity: 80, foodValue: 50, icon: "🍖" },
        { id: "raw_meat", name: "Raw Meat", affinity: 30, foodValue: 50, icon: "🥩" }
      ],
      tamingItems: [
        { name: "Bola (Crucial)", quantity: "2" },
        { name: "Crossbow or Longneck Rifle with Tranq Shots", quantity: "5-10 shots" },
        { name: "Narcotics", quantity: "30-80 (Rapid drain)" }
      ],
      strategy: [
        "**Catch it Grounded**: Wait for the Pteranodon to land on a beach to rest. Approach quietly from behind.",
        "**Throw a Bola**: Throw a Bola to trap it on the ground. You have 30 seconds to knock it out before it breaks free and flies away forever.",
        "**Headshots only**: Shoot it in the head with tranquilizers. Pteranodons have a massive 3x headshot multiplier, meaning 1 or 2 tranq arrows will knock them out instantly.",
        "**Fast Torpor Drain**: WARNING! Pteranodons lose torpor very quickly. Do not leave them unattended; keep a close eye on their unconscious bar and feed them Narcotics immediately."
      ],
      utility: [
        "**Aerial Travel**: The earliest available flyer. Bypasses ground predators and rough terrain entirely.",
        "**Barrel Roll**: By pressing the alternative fire key while flying, they perform a spinning dive attack that deals high damage and travels forward quickly.",
        "**Pick Up**: Can carry small creatures (Dodos, Dilophosaurs) and transport them to taming pens."
      ]
    }
  },
  {
    id: "spino",
    name: "Spinosaurus",
    tagline: "The Leviathan of the Swamps",
    diet: "Carnivore",
    tameType: "Knockout",
    class: "Fighter",
    image: "assets/images/spino.png",
    description: "Spinosaurus aquaregina is a massive, semi-aquatic predator that patrols river systems. They are faster than a Rex on flat land and gain a massive hydration buff when touching water. Spinos can stand on their hind legs to swipe at enemies, making them highly versatile war mounts.",
    baseStats: {
      health: 700,
      stamina: 350,
      oxygen: 650,
      food: 2600,
      weight: 350,
      melee: 40,
      speed: 100,
      torpidity: 850
    },
    taming: {
      baseAffinity: 1600,
      affinityPerLevel: 80,
      baseTorpor: 850,
      torporPerLevel: 51,
      baseTorporDrain: 2.33, // Extremely fast drain!
      torporDrainPerLevel: 0.18,
      foodDrainRate: 0.750,
      preferredFoods: [
        { id: "exceptional_kibble", name: "Exceptional Kibble", affinity: 350, foodValue: 80, icon: "🥚" },
        { id: "raw_mutton", name: "Raw Mutton", affinity: 180, foodValue: 50, icon: "🥩" },
        { id: "raw_prime_fish", name: "Raw Prime Fish Meat", affinity: 160, foodValue: 50, icon: "🐟" },
        { id: "raw_prime_meat", name: "Raw Prime Meat", affinity: 120, foodValue: 50, icon: "🍖" },
        { id: "raw_meat", name: "Raw Meat", affinity: 40, foodValue: 50, icon: "🥩" }
      ],
      tamingItems: [
        { name: "Stone Behemoth Gateway & Gates", quantity: "3 gateways, 2 gates" },
        { name: "Large Bear Trap", quantity: "2" },
        { name: "Longneck Rifle with Tranq Darts", quantity: "30-60 shots" },
        { name: "Narcotics (CRITICAL)", quantity: "300-600" }
      ],
      strategy: [
        "**Prepare for Fast Torpor**: Spinos have a legendary torpor drain speed. If you run out of Narcotics, they will wake up in a matter of seconds. Have all your supplies ready before pulling.",
        "**Build a Gateway Pen**: Spinos can climb over small walls and run incredibly fast when low on health. Trapping them in a stone behemoth gate pen is highly recommended.",
        "**Pull out of Water**: Never shoot a Spino while it is in the water. If it falls unconscious in deep water, it will drown instantly. Lure it onto a dry riverbank before knocking it out."
      ],
      utility: [
        "**Hydrated Fighter**: Gains +15% damage, +20% movement speed, and +20% health regeneration for 30 seconds after touching water.",
        "**Bipedal Stance**: Can toggle between walking on four legs (fast running) and standing on two legs (tighter turning radius, +20% damage swipes).",
        "**All-Terrain Predator**: Excellent swimmer and climber, making it easier to maneuver than a Rex."
      ]
    }
  },
  {
    id: "anky",
    name: "Ankylosaurus",
    tagline: "The Walking Quarry of the Mountains",
    diet: "Herbivore",
    tameType: "Knockout",
    class: "Harvester",
    image: "assets/images/anky.png",
    description: "Ankylosaurus magnacutator is a slow-moving, heavily armored herbivore covered in bone spikes, ending in a thick clubbed tail. They are typically docile but lash out with their spiked tail when provoked. Tamed Ankylos are indispensable for gathering metal, obsidian, and flint.",
    baseStats: {
      health: 700,
      stamina: 175,
      oxygen: 150,
      food: 3000,
      weight: 250,
      melee: 50,
      speed: 100,
      torpidity: 420
    },
    taming: {
      baseAffinity: 1200,
      affinityPerLevel: 60,
      baseTorpor: 420,
      torporPerLevel: 25.2,
      baseTorporDrain: 0.28,
      torporDrainPerLevel: 0.02,
      foodDrainRate: 0.250,
      preferredFoods: [
        { id: "regular_kibble", name: "Regular Kibble", affinity: 300, foodValue: 80, icon: "🥚" },
        { id: "mejoberry", name: "Mejoberry", affinity: 40, foodValue: 30, icon: "🍇" },
        { id: "crops", name: "Savoroot/Longrass/Citronal/Rockarrot", affinity: 50, foodValue: 40, icon: "🌽" },
        { id: "other_berries", name: "Other Berries", affinity: 20, foodValue: 20, icon: "🍒" }
      ],
      tamingItems: [
        { name: "Wooden Pillars (to block moves)", quantity: "4" },
        { name: "Crossbow with Tranq Arrows", quantity: "15-35 shots" },
        { name: "Mejoberries / Crops", quantity: "100-300" },
        { name: "Narcotics", quantity: "30-80" }
      ],
      strategy: [
        "**Kiting**: The Anky is extremely slow on land. You can easily outrun them by simply walking backwards and firing tranquilizers.",
        "**Aim for Tail and Limbs**: Their shell armor reduces damage taken by 50%, but this does not affect torpor. Just keep firing from a safe distance.",
        "**High Torpor Pool**: Due to their thick armor, they can take a while to drop. Keep a steady pace to avoid killing them."
      ],
      utility: [
        "**Metal Harvester**: Gathers metal at a 5x rate compared to a metal pickaxe. In addition, they have an innate **85% weight reduction** for Metal carried in their inventory.",
        "**Flint & Obsidian**: Gathers large amounts of flint from rocks and obsidian from mountain peaks.",
        "**Flint Combat**: Their heavy tail swipe deals decent knockback and can break enemy armor in PvP."
      ]
    }
  }
];

// Export if in Node context, otherwise define globally
if (typeof module !== "undefined" && module.exports) {
  module.exports = { DINOSAURS_DATA };
} else {
  window.DINOSAURS_DATA = DINOSAURS_DATA;
}
