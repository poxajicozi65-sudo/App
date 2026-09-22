package com.example.data.templates

import com.example.data.model.BotButton
import com.example.data.model.BotFlow
import com.example.data.model.BotNode
import com.example.data.model.BotNodeType

data class BotTemplate(
    val id: String,
    val title: String,
    val category: String,
    val icon: String,
    val description: String,
    val badge: String,
    val flow: BotFlow
)

object BotTemplates {

    val ALL_TEMPLATES: List<BotTemplate> by lazy {
        listOf(
            createEarnFastTemplate(),
            createRealCashLudoTemplate(),
            createGameToZoneTemplate(),
            createCashRewardTemplate(),
            createTapToEarnTemplate(),
            createAiSupportTemplate(),
            createDigitalStoreTemplate(),
            createCryptoPriceTrackerTemplate(),
            createQuizMasterTemplate(),
            createVipSubscriptionTemplate()
        )
    }

    private fun createEarnFastTemplate(): BotTemplate {
        val nodes = listOf(
            BotNode(
                id = "node_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                posX = 40f,
                posY = 60f,
                nextNodeId = "node_welcome"
            ),
            BotNode(
                id = "node_welcome",
                title = "Welcome & EarnFast Intro",
                type = BotNodeType.MESSAGE,
                content = "⚡ Welcome to EarnFast - Play & Win!\n\n🎮 Play Image Finder, Tic Tac Toe, and Math Quiz\n💰 Earn real coins and cash out to UPI/PayPal/Crypto\n👥 Invite friends to earn +50 Coins bonus instantly!\n\nTap below to launch the Mini App:",
                posX = 40f,
                posY = 240f,
                nextNodeId = "node_buttons"
            ),
            BotNode(
                id = "node_buttons",
                title = "EarnFast Buttons",
                type = BotNodeType.INLINE_BUTTONS,
                content = "EarnFast Main Menu",
                buttons = listOf(
                    BotButton(text = "🚀 Launch Mini App", type = "webapp", payload = "open_earnfast"),
                    BotButton(text = "🎁 Daily Bonus (+20)", type = "callback", payload = "claim_bonus"),
                    BotButton(text = "👥 Invite Friends", type = "callback", payload = "share_ref")
                ),
                posX = 40f,
                posY = 440f
            ),
            BotNode(
                id = "node_games",
                title = "Mini Games Hub",
                type = BotNodeType.WEBAPP_VIEW,
                content = "Image Finder (3-streak match) + Smart Tic-Tac-Toe AI + Math Solve Quiz with real-time online presence counter.",
                posX = 340f,
                posY = 200f
            )
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EarnFast - Play & Win</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <script src="https://telegram.org/js/telegram-web-app.js"></script>
    <style>
        body { background-color: #0f172a; color: #fff; font-family: sans-serif; user-select: none; }
        .glass-card { background: rgba(30, 41, 59, 0.7); backdrop-filter: blur(10px); border: 1px solid rgba(255, 255, 255, 0.1); }
    </style>
</head>
<body class="p-4 flex flex-col min-h-screen">
    <header class="flex justify-between items-center mb-6">
        <div>
            <h1 class="text-xl font-extrabold text-indigo-400">⚡ EarnFast</h1>
            <p class="text-xs text-slate-400">Play & Win Coins</p>
        </div>
        <div class="glass-card px-3 py-1.5 rounded-full flex items-center gap-2 border border-yellow-500/30">
            <i class="fas fa-coins text-yellow-400 text-sm"></i>
            <span class="font-bold text-white text-sm" id="coins-disp">100</span>
        </div>
    </header>

    <div class="glass-card p-4 rounded-2xl mb-6 bg-gradient-to-r from-indigo-900/60 to-purple-900/60 border-indigo-500/30">
        <span class="text-[10px] bg-red-500 text-white font-bold px-2 py-0.5 rounded uppercase">Featured</span>
        <h2 class="text-xl font-bold mt-1">Image Finder 3x Streak</h2>
        <p class="text-xs text-indigo-200 mb-3">Find matching icons within 30s to claim +20 Coins!</p>
        <button onclick="playImageFinder()" class="bg-indigo-600 hover:bg-indigo-500 text-white font-bold px-4 py-2 rounded-xl text-xs active:scale-95 transition">Play Now</button>
    </div>

    <div class="grid grid-cols-2 gap-3 mb-6">
        <div class="glass-card p-4 rounded-xl text-center active:scale-95 transition cursor-pointer" onclick="playTicTacToe()">
            <div class="text-3xl mb-2">❌</div>
            <h3 class="font-bold text-sm">Tic Tac Toe</h3>
            <p class="text-[10px] text-slate-400">Beat AI (+15)</p>
        </div>
        <div class="glass-card p-4 rounded-xl text-center active:scale-95 transition cursor-pointer" onclick="playMath()">
            <div class="text-3xl mb-2">➗</div>
            <h3 class="font-bold text-sm">Math Quiz</h3>
            <p class="text-[10px] text-slate-400">Solve Quick (+10)</p>
        </div>
    </div>

    <div class="mt-auto glass-card p-4 rounded-xl flex justify-between items-center">
        <div>
            <h4 class="font-bold text-sm">Redeem Rewards</h4>
            <p class="text-[10px] text-slate-400">1000 Coins = ₹10</p>
        </div>
        <button onclick="alert('Withdrawal Request Sent!')" class="bg-emerald-600 hover:bg-emerald-500 px-4 py-2 rounded-xl text-xs font-bold active:scale-95">Withdraw</button>
    </div>

    <script>
        let coins = 100;
        function playImageFinder() {
            coins += 20;
            document.getElementById('coins-disp').innerText = coins;
            alert('🎉 Great Job! 3-streak solved! +20 Coins added!');
        }
        function playTicTacToe() {
            coins += 15;
            document.getElementById('coins-disp').innerText = coins;
            alert('🎉 Smart move! Beat the AI: +15 Coins added!');
        }
        function playMath() {
            coins += 10;
            document.getElementById('coins-disp').innerText = coins;
            alert('🎉 Correct solution! +10 Coins added!');
        }
    </script>
</body>
</html>
        """.trimIndent()

        return BotTemplate(
            id = "tpl_earnfast",
            title = "EarnFast — Play & Win Bot",
            category = "Gaming & Tap",
            icon = "⚡",
            description = "Image Finder game, Smart AI Tic Tac Toe, Math solve quiz, real-time presence counter, Monetag Ads, and instant coin-to-rupee payout gateway.",
            badge = "Source Preset",
            flow = BotFlow(
                name = "EarnFast Bot",
                username = "earnfast_play_bot",
                description = "Play games, complete quizzes, and cash out coins",
                nodes = nodes,
                webappHtml = html
            )
        )
    }

    private fun createRealCashLudoTemplate(): BotTemplate {
        val nodes = listOf(
            BotNode(
                id = "node_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                posX = 40f,
                posY = 60f,
                nextNodeId = "node_welcome"
            ),
            BotNode(
                id = "node_welcome",
                title = "Welcome & Ludo Arena",
                type = BotNodeType.MESSAGE,
                content = "🎲 Welcome to RealCash Ludo & Arena!\n\n🏆 Challenge opponents in 1v1 Quick Ludo\n🎡 Spin the wheel & scratch cards daily\n💰 Earn coins and withdraw instantly!\n\nTap below to open the Arena:",
                posX = 40f,
                posY = 240f,
                nextNodeId = "node_buttons"
            ),
            BotNode(
                id = "node_buttons",
                title = "Ludo Menu Buttons",
                type = BotNodeType.INLINE_BUTTONS,
                content = "Arena Menu",
                buttons = listOf(
                    BotButton(text = "🎲 Play 1v1 Ludo", type = "webapp", payload = "open_ludo"),
                    BotButton(text = "🎡 Daily Spin Wheel", type = "webapp", payload = "open_spin"),
                    BotButton(text = "🎁 Scratch Card", type = "webapp", payload = "open_scratch")
                ),
                posX = 40f,
                posY = 440f
            ),
            BotNode(
                id = "node_arena",
                title = "Interactive Ludo Canvas",
                type = BotNodeType.WEBAPP_VIEW,
                content = "Full React 18 1v1 Ludo board canvas with coordinates, safe zones, dice animations, and leaderboards.",
                posX = 340f,
                posY = 200f
            )
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RealCash Ludo Arena</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <script src="https://telegram.org/js/telegram-web-app.js"></script>
</head>
<body class="bg-slate-900 text-white p-4 min-h-screen flex flex-col justify-between">
    <div>
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-xl font-black text-indigo-400">🎲 RealCash Ludo</h1>
            <span class="bg-amber-500/20 text-yellow-400 font-bold px-3 py-1 rounded-full text-xs border border-yellow-500/30">⭐ 250 Coins</span>
        </div>
        <div class="bg-slate-800 p-4 rounded-2xl border border-slate-700 mb-4 text-center">
            <p class="text-xs text-slate-400 mb-1">1v1 Quick Ludo Battle</p>
            <div class="w-48 h-48 mx-auto my-3 bg-slate-700 rounded-2xl flex items-center justify-center border-4 border-indigo-500 shadow-xl">
                <span class="text-6xl">🎲</span>
            </div>
            <button onclick="alert('Dice Rolled: 6! Moved pawn safely.')" class="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-3 rounded-xl active:scale-95 transition">Roll Dice</button>
        </div>
        <div class="grid grid-cols-2 gap-3">
            <button onclick="alert('Spin Wheel Opened! Won +15 Coins!')" class="bg-slate-800 p-4 rounded-xl border border-slate-700 font-bold text-xs flex items-center justify-center gap-2">🎡 Spin Wheel</button>
            <button onclick="alert('Scratch Card Scratched! Won +20 Coins!')" class="bg-slate-800 p-4 rounded-xl border border-slate-700 font-bold text-xs flex items-center justify-center gap-2">🎁 Scratch Card</button>
        </div>
    </div>
    <div class="bg-slate-800/80 p-3 rounded-xl flex justify-between items-center text-xs">
        <span class="text-slate-400">Conversion: 1000 Coins = ₹10</span>
        <button onclick="alert('Withdrawal Request Sent!')" class="bg-green-600 px-3 py-1.5 rounded-lg font-bold">Redeem</button>
    </div>
</body>
</html>
        """.trimIndent()

        return BotTemplate(
            id = "tpl_realcash",
            title = "RealCash — 1v1 Ludo Arena",
            category = "Gaming & Tap",
            icon = "🎲",
            description = "Interactive 1v1 Ludo board game canvas with turn timers, dice physics, Tic-Tac-Toe AI, Spin Wheel, and Scratch card rewards.",
            badge = "Source Preset",
            flow = BotFlow(
                name = "RealCash Bot",
                username = "realcash_ludo_bot",
                description = "1v1 Ludo, Spin & Scratch battle bot",
                nodes = nodes,
                webappHtml = html
            )
        )
    }

    private fun createGameToZoneTemplate(): BotTemplate {
        val nodes = listOf(
            BotNode(
                id = "node_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                posX = 40f,
                posY = 60f,
                nextNodeId = "node_welcome"
            ),
            BotNode(
                id = "node_welcome",
                title = "Welcome & Arcade",
                type = BotNodeType.MESSAGE,
                content = "🎮 Welcome to GameToZone!\n\n🕹️ Play top HTML5 web games\n⏱️ Play 30s to unlock Claim Coins reward\n📺 Watch video ads with cooldown timer\n\nTap below to launch Arcade:",
                posX = 40f,
                posY = 240f,
                nextNodeId = "node_buttons"
            ),
            BotNode(
                id = "node_buttons",
                title = "Game Menu Buttons",
                type = BotNodeType.INLINE_BUTTONS,
                content = "Arcade Hub",
                buttons = listOf(
                    BotButton(text = "🎮 Play HTML5 Games", type = "webapp", payload = "open_games"),
                    BotButton(text = "📺 Watch & Earn", type = "callback", payload = "watch_ad"),
                    BotButton(text = "💳 Cashout Wallet", type = "callback", payload = "open_wallet")
                ),
                posX = 40f,
                posY = 440f
            ),
            BotNode(
                id = "node_arcade",
                title = "HTML5 Game Embed",
                type = BotNodeType.WEBAPP_VIEW,
                content = "Embedded iframe games launcher with countdown play timer and pulsing claim button.",
                posX = 340f,
                posY = 200f
            )
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>GameToZone Arcade</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://telegram.org/js/telegram-web-app.js"></script>
</head>
<body class="bg-slate-900 text-white p-4 min-h-screen">
    <div class="flex justify-between items-center mb-6">
        <h1 class="text-xl font-bold text-orange-400">🎮 GameToZone</h1>
        <span class="bg-yellow-500/20 text-yellow-300 font-bold px-3 py-1 rounded-full text-xs">🪙 150 Coins</span>
    </div>
    <div class="bg-gradient-to-r from-orange-600 to-red-600 p-5 rounded-2xl mb-4 text-center">
        <h2 class="text-xl font-bold mb-1">Play HTML5 Games</h2>
        <p class="text-xs text-orange-100 mb-4">Play for 30 seconds to unlock +50 Coins bonus!</p>
        <button onclick="alert('Game Started! Play 30s to unlock Claim Coins.')" class="bg-white text-orange-600 font-bold px-5 py-2.5 rounded-full text-xs active:scale-95">Play Now</button>
    </div>
    <div class="bg-slate-800 p-4 rounded-xl border border-slate-700 flex justify-between items-center">
        <div>
            <h3 class="font-bold text-sm">Watch & Earn Video Ad</h3>
            <p class="text-xs text-yellow-400">+20 Coins (15s cooldown)</p>
        </div>
        <button onclick="alert('Watched Ad! +20 Coins Claimed!')" class="bg-indigo-600 text-white text-xs font-bold px-4 py-2 rounded-lg">Watch</button>
    </div>
</body>
</html>
        """.trimIndent()

        return BotTemplate(
            id = "tpl_gametozone",
            title = "GameToZone — Arcade Games Bot",
            category = "Gaming & Tap",
            icon = "🎮",
            description = "Iframe HTML5 web games with 30s unlock timer, Claim Coins pulse button, Watch & Earn with ad cooldown countdown.",
            badge = "Source Preset",
            flow = BotFlow(
                name = "GameToZone Bot",
                username = "gametozone_arcade_bot",
                description = "HTML5 games player and watch-to-earn bot",
                nodes = nodes,
                webappHtml = html
            )
        )
    }

    private fun createCashRewardTemplate(): BotTemplate {
        val nodes = listOf(
            BotNode(
                id = "node_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                posX = 40f,
                posY = 60f,
                nextNodeId = "node_welcome"
            ),
            BotNode(
                id = "node_welcome",
                title = "Welcome & CashReward",
                type = BotNodeType.MESSAGE,
                content = "🎨 Welcome to CashReward!\n\n🎯 Guess the Color RGB game\n🌐 Visit websites & complete social tasks\n💎 Watch ads & complete 60s special tasks\n\nTap below to open CashReward:",
                posX = 40f,
                posY = 240f,
                nextNodeId = "node_buttons"
            ),
            BotNode(
                id = "node_buttons",
                title = "CashReward Menu",
                type = BotNodeType.INLINE_BUTTONS,
                content = "Task Menu",
                buttons = listOf(
                    BotButton(text = "🎨 Play Color Game", type = "webapp", payload = "open_colorgame"),
                    BotButton(text = "🌐 Visit & Earn", type = "webapp", payload = "open_visit"),
                    BotButton(text = "👥 Social Follow Tasks", type = "callback", payload = "open_social")
                ),
                posX = 40f,
                posY = 440f
            ),
            BotNode(
                id = "node_tasks",
                title = "Color Game & Tasks",
                type = BotNodeType.WEBAPP_VIEW,
                content = "RGB Color guesser with randomized grids, visit and earn tracker, and instant wallet redemption.",
                posX = 340f,
                posY = 200f
            )
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CashReward</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://telegram.org/js/telegram-web-app.js"></script>
</head>
<body class="bg-neutral-900 text-white p-4 min-h-screen">
    <div class="flex justify-between items-center mb-6">
        <h1 class="text-xl font-bold text-yellow-400">🎨 CashReward</h1>
        <span class="bg-yellow-400 text-black font-bold px-3 py-1 rounded-full text-xs">🪙 180 Coins</span>
    </div>
    <div class="bg-neutral-800 p-4 rounded-2xl mb-4 border border-neutral-700">
        <h2 class="font-bold text-sm mb-1">Guess the Color RGB</h2>
        <div class="w-20 h-10 rounded-lg mx-auto my-3 bg-emerald-500 border-2 border-white"></div>
        <p class="text-xs text-neutral-400 text-center mb-3">Target: RGB(16, 185, 129)</p>
        <div class="grid grid-cols-3 gap-2">
            <button onclick="alert('Correct Color! +10 Coins added!')" class="h-12 rounded-xl bg-emerald-500"></button>
            <button onclick="alert('Wrong color!')" class="h-12 rounded-xl bg-indigo-500"></button>
            <button onclick="alert('Wrong color!')" class="h-12 rounded-xl bg-rose-500"></button>
        </div>
    </div>
    <div class="space-y-2">
        <div class="bg-neutral-800 p-3 rounded-xl flex justify-between items-center text-xs">
            <span>🌐 Visit Sponsor Site (15s)</span>
            <button onclick="alert('Visited! +15 Coins Claimed!')" class="bg-yellow-400 text-black font-bold px-3 py-1.5 rounded-lg">Visit</button>
        </div>
        <div class="bg-neutral-800 p-3 rounded-xl flex justify-between items-center text-xs">
            <span>📢 Join Telegram Channel</span>
            <button onclick="alert('Joined! +50 Coins Claimed!')" class="bg-yellow-400 text-black font-bold px-3 py-1.5 rounded-lg">Join</button>
        </div>
    </div>
</body>
</html>
        """.trimIndent()

        return BotTemplate(
            id = "tpl_cashreward",
            title = "CashReward — Color RGB & Tasks",
            category = "E-Commerce",
            icon = "🎨",
            description = "Guess the Color RGB game, 60s special bonus click tasks, Visit & Earn websites, and Social follow task rewards.",
            badge = "Source Preset",
            flow = BotFlow(
                name = "CashReward Bot",
                username = "cashreward_task_bot",
                description = "Color games and reward task bot",
                nodes = nodes,
                webappHtml = html
            )
        )
    }

    private fun createTapToEarnTemplate(): BotTemplate {
        val nodes = listOf(
            BotNode(
                id = "node_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                posX = 40f,
                posY = 60f,
                nextNodeId = "node_welcome"
            ),
            BotNode(
                id = "node_welcome",
                title = "Welcome & Tap Intro",
                type = BotNodeType.MESSAGE,
                content = "🐹 Welcome to Hamster Gold Miner!\n\nTap the gold coin to mine tokens, upgrade your pickaxe speed, and invite friends for a +5,000 bonus!\n\nTap below to launch the Mini App:",
                posX = 40f,
                posY = 240f,
                nextNodeId = "node_inline_buttons"
            ),
            BotNode(
                id = "node_inline_buttons",
                title = "Launch & Menu Buttons",
                type = BotNodeType.INLINE_BUTTONS,
                content = "Mining Menu",
                buttons = listOf(
                    BotButton(text = "🚀 Launch Mining MiniApp", type = "webapp", payload = "open_miniapp"),
                    BotButton(text = "🤝 Invite Friends (+5,000)", type = "callback", payload = "invite_friends"),
                    BotButton(text = "🏆 Leaderboard", type = "callback", payload = "view_leaderboard")
                ),
                posX = 40f,
                posY = 440f,
                nextNodeId = "node_action_claim"
            ),
            BotNode(
                id = "node_action_claim",
                title = "Daily Streak Bonus",
                type = BotNodeType.ACTION_LOGIC,
                content = "Checks user 24h cooldown and awards +100 gold coins instantly to Firestore balance.",
                posX = 320f,
                posY = 140f
            ),
            BotNode(
                id = "node_miniapp",
                title = "Gold Miner Mini App",
                type = BotNodeType.WEBAPP_VIEW,
                content = "Full-screen HTML5 physics coin clicker with combo particle sparks & energy bar.",
                posX = 320f,
                posY = 360f
            )
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>Hamster Gold Miner</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; user-select: none; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    body { background: #0f172a; color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: space-between; min-height: 100vh; padding: 20px; overflow: hidden; }
    .header { text-align: center; width: 100%; margin-top: 10px; }
    .badge { display: inline-block; background: rgba(245, 158, 11, 0.2); border: 1px solid #f59e0b; color: #f59e0b; padding: 4px 14px; border-radius: 20px; font-size: 13px; font-weight: 700; }
    .balance-box { margin-top: 14px; display: flex; align-items: center; justify-content: center; gap: 10px; }
    .balance-box .coin-icon { font-size: 38px; }
    .balance-text { font-size: 40px; font-weight: 900; color: #fbbf24; text-shadow: 0 0 20px rgba(251, 191, 36, 0.4); }
    .center-stage { position: relative; margin: auto 0; }
    .coin-btn { width: 220px; height: 220px; border-radius: 50%; background: radial-gradient(circle at 35% 35%, #fde047, #f59e0b 60%, #b45309 100%); border: 8px solid #fbbf24; box-shadow: 0 10px 40px rgba(245, 158, 11, 0.5), inset 0 -6px 14px rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; font-size: 86px; cursor: pointer; transition: transform 0.08s ease; -webkit-tap-highlight-color: transparent; }
    .coin-btn:active { transform: scale(0.92); }
    .floating-num { position: absolute; font-size: 26px; font-weight: 900; color: #fef08a; pointer-events: none; animation: floatUp 0.8s ease-out forwards; }
    @keyframes floatUp { 0% { opacity: 1; transform: translateY(0) scale(1); } 100% { opacity: 0; transform: translateY(-70px) scale(1.3); } }
    .footer { width: 100%; max-width: 340px; margin-bottom: 10px; }
    .energy-bar-wrap { width: 100%; height: 16px; background: #1e293b; border-radius: 10px; overflow: hidden; border: 1px solid #334155; margin-bottom: 8px; }
    .energy-bar-fill { height: 100%; width: 100%; background: linear-gradient(90deg, #10b981, #34d399); transition: width 0.2s ease; }
    .energy-meta { display: flex; justify-content: space-between; font-size: 13px; color: #94a3b8; font-weight: 600; }
    .stats-row { display: flex; gap: 10px; margin-top: 14px; }
    .stat-pill { flex: 1; background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 10px; text-align: center; }
    .stat-pill div:first-child { font-size: 11px; color: #94a3b8; }
    .stat-pill div:last-child { font-size: 16px; font-weight: 700; color: #38bdf8; margin-top: 2px; }
  </style>
</head>
<body>
  <div class="header">
    <div class="badge">SEASON 1 LIVE</div>
    <div class="balance-box">
      <span class="coin-icon">🪙</span>
      <span class="balance-text" id="balance">1,250</span>
    </div>
  </div>

  <div class="center-stage" id="stage">
    <div class="coin-btn" id="coin">🐹</div>
  </div>

  <div class="footer">
    <div class="energy-bar-wrap">
      <div class="energy-bar-fill" id="energyFill"></div>
    </div>
    <div class="energy-meta">
      <span>⚡ Energy</span>
      <span id="energyText">1000 / 1000</span>
    </div>

    <div class="stats-row">
      <div class="stat-pill">
        <div>PROFIT / HOUR</div>
        <div>+1.2K</div>
      </div>
      <div class="stat-pill">
        <div>TAP LEVEL</div>
        <div>LVL 3 (+3)</div>
      </div>
    </div>
  </div>

  <script>
    let coins = 1250;
    let energy = 1000;
    const maxEnergy = 1000;
    const balanceEl = document.getElementById('balance');
    const energyFill = document.getElementById('energyFill');
    const energyText = document.getElementById('energyText');
    const coinEl = document.getElementById('coin');
    const stageEl = document.getElementById('stage');

    coinEl.addEventListener('pointerdown', (e) => {
      if (energy <= 0) return;
      coins += 3;
      energy = Math.max(0, energy - 3);
      balanceEl.textContent = coins.toLocaleString();
      energyFill.style.width = (energy / maxEnergy * 100) + '%';
      energyText.textContent = energy + ' / ' + maxEnergy;

      // Particle text
      const floatEl = document.createElement('div');
      floatEl.className = 'floating-num';
      floatEl.textContent = '+3';
      const rect = coinEl.getBoundingClientRect();
      floatEl.style.left = (e.clientX - rect.left - 10) + 'px';
      floatEl.style.top = (e.clientY - rect.top - 20) + 'px';
      stageEl.appendChild(floatEl);
      setTimeout(() => floatEl.remove(), 800);

      // Telegram haptic if available
      if (window.Telegram && window.Telegram.WebApp && window.Telegram.WebApp.HapticFeedback) {
        window.Telegram.WebApp.HapticFeedback.impactOccurred('medium');
      }
    });

    setInterval(() => {
      if (energy < maxEnergy) {
        energy = Math.min(maxEnergy, energy + 2);
        energyFill.style.width = (energy / maxEnergy * 100) + '%';
        energyText.textContent = energy + ' / ' + maxEnergy;
      }
    }, 1000);
  </script>
</body>
</html>
        """.trimIndent()

        return BotTemplate(
            id = "tpl_tap_to_earn",
            title = "Tap-to-Earn Gold Miner",
            category = "Gaming & Tap",
            icon = "🐹",
            description = "Viral Hamster/Notcoin style clicker bot with particle sparks, energy recharge, upgrades, and Telegram WebApp integration.",
            badge = "Popular",
            flow = BotFlow(
                name = "Hamster Gold Miner",
                username = "hamster_miner_bot",
                description = "Tap to earn gold coins and compete on the global leaderboard",
                nodes = nodes,
                webappHtml = html
            )
        )
    }

    private fun createAiSupportTemplate(): BotTemplate {
        val nodes = listOf(
            BotNode(
                id = "node_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                posX = 40f,
                posY = 60f,
                nextNodeId = "node_welcome"
            ),
            BotNode(
                id = "node_welcome",
                title = "Support Greeting",
                type = BotNodeType.MESSAGE,
                content = "👋 Hello! I am the automated AI Customer Support Assistant.\n\nHow can I help you today? Please choose an inquiry topic or describe your problem:",
                posX = 40f,
                posY = 240f,
                nextNodeId = "node_topics"
            ),
            BotNode(
                id = "node_topics",
                title = "Inquiry Categories",
                type = BotNodeType.INLINE_BUTTONS,
                content = "Support Categories",
                buttons = listOf(
                    BotButton(text = "📦 Order & Shipping Status", type = "callback", payload = "faq_orders"),
                    BotButton(text = "💳 Billing & Refunds", type = "callback", payload = "faq_refunds"),
                    BotButton(text = "💬 Live Human Agent", type = "callback", payload = "live_agent"),
                    BotButton(text = "📖 Open Knowledge Base", type = "webapp", payload = "open_kb")
                ),
                posX = 40f,
                posY = 450f
            ),
            BotNode(
                id = "node_agent_action",
                title = "Ticket Dispatcher",
                type = BotNodeType.ACTION_LOGIC,
                content = "Generates a support ticket #TX-9021 and alerts staff channel via Telegram webhook.",
                posX = 340f,
                posY = 240f
            )
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Knowledge Base</title>
  <style>
    body { background: #0b132b; color: #fff; font-family: -apple-system, sans-serif; padding: 20px; }
    .header { font-size: 22px; font-weight: bold; margin-bottom: 6px; }
    .sub { color: #94a3b8; font-size: 13px; margin-bottom: 20px; }
    .search-box { width: 100%; padding: 12px; background: #1c2541; border: 1px solid #3a506b; border-radius: 10px; color: #fff; margin-bottom: 20px; outline: none; }
    .item { background: #1c2541; border-radius: 12px; padding: 16px; margin-bottom: 12px; }
    .item h4 { color: #6fffe9; font-size: 15px; margin-bottom: 6px; }
    .item p { color: #cbd5e1; font-size: 13px; line-height: 1.5; }
  </style>
</head>
<body>
  <div class="header">Support Center</div>
  <div class="sub">Find answers to common questions</div>
  <input class="search-box" placeholder="Search answers...">
  <div class="item">
    <h4>How do I track my order?</h4>
    <p>You can track delivery in real time by clicking 'Order Status' or entering your 8-digit tracking ID.</p>
  </div>
  <div class="item">
    <h4>What is the refund policy?</h4>
    <p>We provide full no-questions-asked refunds within 14 calendar days of delivery.</p>
  </div>
</body>
</html>
        """.trimIndent()

        return BotTemplate(
            id = "tpl_ai_support",
            title = "AI Customer Support Bot",
            category = "Customer Service",
            icon = "🤖",
            description = "Intelligent customer service bot with FAQ classification, ticket routing, live human agent escalation, and interactive help desk.",
            badge = "Utility",
            flow = BotFlow(
                name = "AI Support Desk",
                username = "support_ai_bot",
                description = "24/7 automated customer assistance and ticket router",
                nodes = nodes,
                webappHtml = html
            )
        )
    }

    private fun createDigitalStoreTemplate(): BotTemplate {
        val nodes = listOf(
            BotNode(
                id = "node_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                posX = 40f,
                posY = 60f,
                nextNodeId = "node_shop_welcome"
            ),
            BotNode(
                id = "node_shop_welcome",
                title = "Store Front",
                type = BotNodeType.MESSAGE,
                content = "🛍️ Welcome to Stars Digital Shop!\n\nBrowse our exclusive collection of digital assets, game keys, and VIP memberships. Pay safely using Telegram Stars or TON:",
                posX = 40f,
                posY = 240f,
                nextNodeId = "node_shop_buttons"
            ),
            BotNode(
                id = "node_shop_buttons",
                title = "Catalog Buttons",
                type = BotNodeType.INLINE_BUTTONS,
                content = "Store Navigation",
                buttons = listOf(
                    BotButton(text = "🛒 Open Digital Catalog", type = "webapp", payload = "open_store"),
                    BotButton(text = "⭐ Buy Telegram Stars", type = "callback", payload = "buy_stars"),
                    BotButton(text = "📦 My Orders", type = "callback", payload = "my_orders")
                ),
                posX = 40f,
                posY = 440f
            ),
            BotNode(
                id = "node_stars_checkout",
                title = "Stars Invoice Creator",
                type = BotNodeType.PAYMENT_STARS,
                content = "Generates Telegram Stars Invoice payload with item title and amount.",
                posX = 330f,
                posY = 240f
            )
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Digital Shop</title>
  <style>
    body { background: #111827; color: #fff; font-family: -apple-system, sans-serif; padding: 16px; margin: 0; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .cart-badge { background: #3b82f6; padding: 6px 12px; border-radius: 16px; font-size: 13px; font-weight: bold; }
    .card { background: #1f2937; border-radius: 14px; padding: 14px; margin-bottom: 12px; border: 1px solid #374151; display: flex; justify-content: space-between; align-items: center; }
    .item-title { font-weight: bold; font-size: 15px; }
    .item-price { color: #f59e0b; font-size: 14px; font-weight: bold; margin-top: 4px; }
    .buy-btn { background: #3b82f6; color: white; border: none; padding: 8px 16px; border-radius: 10px; font-weight: bold; cursor: pointer; }
  </style>
</head>
<body>
  <div class="header">
    <h2>Stars Catalog</h2>
    <div class="cart-badge" id="cart">Cart: 0 ⭐</div>
  </div>
  <div class="card">
    <div>
      <div class="item-title">👑 VIP 30-Day Pass</div>
      <div class="item-price">⭐ 250 Stars</div>
    </div>
    <button class="buy-btn" onclick="addToCart(250)">Add</button>
  </div>
  <div class="card">
    <div>
      <div class="item-title">⚡ 5,000 Game Coins</div>
      <div class="item-price">⭐ 100 Stars</div>
    </div>
    <button class="buy-btn" onclick="addToCart(100)">Add</button>
  </div>
  <div class="card">
    <div>
      <div class="item-title">💎 Special Avatar NFT</div>
      <div class="item-price">⭐ 500 Stars</div>
    </div>
    <button class="buy-btn" onclick="addToCart(500)">Add</button>
  </div>

  <script>
    let total = 0;
    function addToCart(amt) {
      total += amt;
      document.getElementById('cart').textContent = 'Cart: ' + total + ' ⭐';
      if (window.Telegram && window.Telegram.WebApp) {
        window.Telegram.WebApp.MainButton.setText('CHECKOUT (' + total + ' ⭐)');
        window.Telegram.WebApp.MainButton.show();
      }
    }
  </script>
</body>
</html>
        """.trimIndent()

        return BotTemplate(
            id = "tpl_digital_store",
            title = "Digital Store & Telegram Stars",
            category = "E-Commerce",
            icon = "🛍️",
            description = "E-Commerce mini bot with catalog browsing, shopping cart, Telegram Stars invoices, and instant digital delivery.",
            badge = "Monetized",
            flow = BotFlow(
                name = "Stars Digital Store",
                username = "stars_shop_bot",
                description = "Digital downloads and Telegram Stars checkout shop",
                nodes = nodes,
                webappHtml = html
            )
        )
    }

    private fun createCryptoPriceTrackerTemplate(): BotTemplate {
        val nodes = listOf(
            BotNode(
                id = "node_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                posX = 40f,
                posY = 60f,
                nextNodeId = "node_crypto_welcome"
            ),
            BotNode(
                id = "node_crypto_welcome",
                title = "Tracker Dashboard",
                type = BotNodeType.MESSAGE,
                content = "📊 TON & Crypto Market Tracker\n\nReal-time price feeds, gas tracker, and customizable portfolio alerts on Telegram.",
                posX = 40f,
                posY = 240f,
                nextNodeId = "node_crypto_buttons"
            ),
            BotNode(
                id = "node_crypto_buttons",
                title = "Price Action Buttons",
                type = BotNodeType.INLINE_BUTTONS,
                content = "Market Feeds",
                buttons = listOf(
                    BotButton(text = "💎 Open Live Charts WebApp", type = "webapp", payload = "open_charts"),
                    BotButton(text = "🔔 Set Price Alert", type = "callback", payload = "set_alert"),
                    BotButton(text = "💼 Portfolio Tracker", type = "callback", payload = "portfolio")
                ),
                posX = 40f,
                posY = 440f
            )
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>TON Crypto Tracker</title>
  <style>
    body { background: #0f172a; color: #fff; font-family: -apple-system, sans-serif; padding: 18px; margin: 0; }
    .title { font-size: 20px; font-weight: bold; margin-bottom: 14px; }
    .ticker { background: #1e293b; border-radius: 14px; padding: 14px; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center; border: 1px solid #334155; }
    .asset { font-weight: bold; font-size: 16px; }
    .price { font-size: 18px; font-weight: 800; }
    .change-pos { color: #10b981; font-size: 12px; font-weight: bold; text-align: right; }
  </style>
</head>
<body>
  <div class="title">Live Crypto Feed</div>
  <div class="ticker">
    <div>
      <div class="asset">💎 TON (The Open Network)</div>
      <div style="color: #94a3b8; font-size: 12px;">Rank #12</div>
    </div>
    <div>
      <div class="price">$5.42</div>
      <div class="change-pos">+4.8% 24h</div>
    </div>
  </div>
  <div class="ticker">
    <div>
      <div class="asset">🪙 Bitcoin (BTC)</div>
      <div style="color: #94a3b8; font-size: 12px;">Rank #1</div>
    </div>
    <div>
      <div class="price">$68,450</div>
      <div class="change-pos">+2.1% 24h</div>
    </div>
  </div>
  <div class="ticker">
    <div>
      <div class="asset">⚡ Ethereum (ETH)</div>
      <div style="color: #94a3b8; font-size: 12px;">Rank #2</div>
    </div>
    <div>
      <div class="price">$3,520</div>
      <div class="change-pos">+3.4% 24h</div>
    </div>
  </div>
</body>
</html>
        """.trimIndent()

        return BotTemplate(
            id = "tpl_crypto_tracker",
            title = "TON & Crypto Market Tracker",
            category = "Crypto & Web3",
            icon = "📈",
            description = "Track TON, BTC, ETH live market prices, set volatility push notifications, and monitor your crypto balances.",
            badge = "Finance",
            flow = BotFlow(
                name = "TON Price Tracker",
                username = "ton_crypto_bot",
                description = "Live prices and portfolio watcher for Telegram",
                nodes = nodes,
                webappHtml = html
            )
        )
    }

    private fun createQuizMasterTemplate(): BotTemplate {
        val nodes = listOf(
            BotNode(
                id = "node_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                posX = 40f,
                posY = 60f,
                nextNodeId = "node_quiz_welcome"
            ),
            BotNode(
                id = "node_quiz_welcome",
                title = "Trivia Challenge",
                type = BotNodeType.MESSAGE,
                content = "🧠 Welcome to Web3 Trivia Master!\n\nAnswer 5 daily trivia questions correctly to win tokens and climb the community leaderboard!",
                posX = 40f,
                posY = 240f,
                nextNodeId = "node_quiz_buttons"
            ),
            BotNode(
                id = "node_quiz_buttons",
                title = "Quiz Action Buttons",
                type = BotNodeType.INLINE_BUTTONS,
                content = "Game Options",
                buttons = listOf(
                    BotButton(text = "🎮 Play Daily Trivia WebApp", type = "webapp", payload = "play_quiz"),
                    BotButton(text = "🏆 Global Leaderboard", type = "callback", payload = "leaderboard")
                ),
                posX = 40f,
                posY = 440f
            )
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Quiz Master</title>
  <style>
    body { background: #1e1b4b; color: #fff; font-family: -apple-system, sans-serif; padding: 20px; }
    .q-box { background: #312e81; border-radius: 16px; padding: 20px; margin-bottom: 20px; }
    .q-text { font-size: 18px; font-weight: bold; margin-bottom: 16px; }
    .opt { background: #4338ca; border: 1px solid #6366f1; border-radius: 12px; padding: 14px; margin-bottom: 10px; cursor: pointer; font-weight: 600; }
    .opt:active { background: #6366f1; }
  </style>
</head>
<body>
  <div class="q-box">
    <div style="color: #a5b4fc; font-size: 13px; font-weight: bold; margin-bottom: 6px;">QUESTION 1 OF 5</div>
    <div class="q-text">What consensus mechanism does TON blockchain use?</div>
    <div class="opt" onclick="alert('Correct! +50 Coins')">A) Proof of Stake (BFT PoS) ✅</div>
    <div class="opt" onclick="alert('Incorrect!')">B) Proof of Work (PoW)</div>
    <div class="opt" onclick="alert('Incorrect!')">C) Proof of History</div>
  </div>
</body>
</html>
        """.trimIndent()

        return BotTemplate(
            id = "tpl_quiz_master",
            title = "Trivia & Quiz Master",
            category = "Games & Quizzes",
            icon = "🧠",
            description = "Interactive timed trivia bot with multiple choices, daily challenges, scoring, and animated reward celebrations.",
            badge = "Interactive",
            flow = BotFlow(
                name = "Web3 Quiz Master",
                username = "quiz_master_bot",
                description = "Daily trivia and rewards for Telegram communities",
                nodes = nodes,
                webappHtml = html
            )
        )
    }

    private fun createVipSubscriptionTemplate(): BotTemplate {
        val nodes = listOf(
            BotNode(
                id = "node_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                posX = 40f,
                posY = 60f,
                nextNodeId = "node_vip_welcome"
            ),
            BotNode(
                id = "node_vip_welcome",
                title = "VIP Lounge Invite",
                type = BotNodeType.MESSAGE,
                content = "👑 Welcome to Alpha VIP Signals!\n\nUnlock private VIP channels, daily trading calls, and premium webinars.",
                posX = 40f,
                posY = 240f,
                nextNodeId = "node_vip_buttons"
            ),
            BotNode(
                id = "node_vip_buttons",
                title = "Subscription Options",
                type = BotNodeType.INLINE_BUTTONS,
                content = "Membership Tiers",
                buttons = listOf(
                    BotButton(text = "⭐ Subscribe (150 Stars / mo)", type = "callback", payload = "sub_monthly"),
                    BotButton(text = "🎟️ Enter Promo Code", type = "callback", payload = "enter_promo"),
                    BotButton(text = "📱 Open VIP Perks Hub", type = "webapp", payload = "open_perks")
                ),
                posX = 40f,
                posY = 440f
            )
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>VIP Perks Hub</title>
  <style>
    body { background: #09090b; color: #fff; font-family: -apple-system, sans-serif; padding: 20px; }
    .gold-badge { color: #facc15; font-size: 13px; font-weight: bold; border: 1px solid #facc15; display: inline-block; padding: 4px 10px; border-radius: 20px; margin-bottom: 10px; }
    .card { background: #18181b; border: 1px solid #27272a; border-radius: 14px; padding: 16px; margin-bottom: 12px; }
    .card h3 { font-size: 16px; margin-bottom: 6px; }
    .card p { color: #a1a1aa; font-size: 13px; line-height: 1.4; }
  </style>
</head>
<body>
  <div class="gold-badge">VIP MEMBERSHIP</div>
  <h2>Alpha Community Perks</h2>
  <div class="card" style="margin-top: 14px;">
    <h3>📈 Real-time Trading Calls</h3>
    <p>Get instant push notifications whenever our analysts take a high-conviction position.</p>
  </div>
  <div class="card">
    <h3>🎙️ Weekly AMA & Mentorship</h3>
    <p>Exclusive voice chats with founder and top community builders.</p>
  </div>
</body>
</html>
        """.trimIndent()

        return BotTemplate(
            id = "tpl_vip_subscription",
            title = "VIP Subscription & Community",
            category = "Community & VIP",
            icon = "👑",
            description = "Monetize your audience with paid channel access, subscription management, promo codes, and automated invite links.",
            badge = "Revenue",
            flow = BotFlow(
                name = "Alpha VIP Club",
                username = "alpha_vip_bot",
                description = "Exclusive Telegram channel access and subscription gate",
                nodes = nodes,
                webappHtml = html
            )
        )
    }
}
