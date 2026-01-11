HOW TO ADD PRODUCT & GAME IMAGES
=================================

GENERAL INSTRUCTIONS:
---------------------
1. Find an image for your product or game
2. Save/Rename the image file to match the EXACT name in the database/code
3. Use .png extension (preferred) or .jpg (fallback)
4. Place the file in this folder: src/main/resources/images/
5. Run: mvn compile && mvn exec:java

The ImageLoader loads .png first, then .jpg as fallback.

NAMING CONVENTION:
------------------
- Image filename = Product name + extension
- Example: If product is "Pho Bo", use "Pho Bo.png"
- Example: If game is "League of Legends", use "League of Legends.png"

GAME NAMES (from GamePanel.java):
---------------------------------
- League of Legends.png
- Dota 2.png
- PUBG.png
- Valorant.png
- FIFA Online 4.png
- CS2.png
- Minecraft.png
- Roblox.png

FOOD NAMES (from database):
---------------------------
- Banh Mi.png
- Com Rang.png
- Bo Kho.png
- Bun Rieu.png
- My Trung.png
- Pho Bo.png
- Bun Cha.png
- Com Tam.png
- Goi Cuon.png
- Nem Ran.png

DRINK NAMES (from database):
----------------------------
- CocaCola.png
- Pepsi.png
- Sprite.png
- Bo Huc.png (or sting.jpg)
- Sting.png
- Tra Da.png
- Ca Phe Sua Da.png

IMAGE SPECIFICATIONS:
---------------------
- Recommended: Square images (1:1 aspect ratio)
- Size: 200x200px or 400x400px for high quality
- The app auto-scales to fit within display area
