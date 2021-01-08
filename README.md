# MineChest
README | [中文文档](README_zh-CN.md)

## What is MineChest
MineChest is a Minecraft server chest plugin for auto-classification and auto-sorting.
MineChest aims to provide players with a more convenient way to store and organize items using chests without destroying the game experience.
This plugin has no database and configuration files, you only need to put MineChest.jar under plugins in the server directory to use it easily.

## Auto-sorting
When you open the box, all items will be neatly arranged in your box without any operation.

## Auto-classification
Automatic classification is a complicated task. Although we have thought of many ways to make the work easier, you still need to do some work to use the automatic classification function.

**Add tags:** You need to stick a sign on the front face of the box, ~~which is an oak sign to be precise~~ now supports signs of all materials. Nothing needs to be written on the sign, and then right-click on the sign with a wooden stick. The sign will search for the contents of the box and Automatically add tags.

**Classification:** Now put an empty box on the ground. Similarly, stick a sign on the front face of the box, write `[classify]` in the first line, and write the Y-axis (height) and XZ-axis ranges in the second line, such as `[5, 20]`, we will discuss the meaning of this value later, and then use the hand-held wooden stick to right-click on this box. All the items that can be classified in this box will be automatically classified into nearby boxes.

----

### Tips:
1. If there is no box with Tags nearby, the item will remain in the box.
2. If the box is not within the scanning range, the item will not be transferred. For example, the range `[5,20]` means that the Y-axis distance does not exceed 5 grids, and the X-axis and Z-axis do not exceed 20 grids.

### Notes:
- **This plugin has not been fully tested, and there may be some compatibility and other unknown issues. Please do not use this plugin on the production server** (although you can do this). If you encounter problems, you can comment in the comment area and issue I will reply as soon as I see it.
