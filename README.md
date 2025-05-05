# Sketchpad UI Emulator

Emulating Sketchpad UI for COS583 course at Princeton University. 

Sketchpad was built in 1963 by Ivan Sutherland, and was a revolutionary computer graphics program that allowed users to 
create digital drawings interactively and edit them using constraint-based rules in real-time. 

In this implementation, constraints are applied in an iterative manner with the objective of error below some set threshold or 
some set max iteration. To satisfy constraints, points are moved in the direction that minimize error, where the direction 
of the move is calculated using geometric definitions of the constraints. 

Drawing instructions: 

(1) To draw a line, click on the button `line`. Then, click anywhere on the canvas to signify the startpoint, and immediately drag the mouse without lifting your finger, to 'stretch' and move the line. This is like dragging a rubber-band around, as mentioned in the Sketchpad's paper and demo. 
(2) To draw an arc, click on the button `arc`. Then, click anywhere on the canvas and immediate drag mouse without lifting your finger, to draw the arc with the radius specified by the distance between the first and second click. This also follows the arc-drawing experience described in Sketchpad's paper and demo. 
(3) To apply a constraint, click on the constraint you want to be applied, for example `parallel`, then click on all the line objects you want to make parallel, and finally click on the constraint button `parallel` again to apply the constraint. The lines that have been clicked on will be pairwise parallel. 

To run the program: 
```
./build.sh
```

References: 

(1) Sketchpad paper: Ivan E. Sutherland. 1963. Sketchpad: a man-machine graphical communication system. In Proceedings of the May 21-23, 1963, spring joint computer conference (AFIPS '63 (Spring)). Association for Computing Machinery, New York, NY, USA, 329–346. https://doi.org/10.1145/1461551.1461591 

(2) Sketchpad demo video 1963: https://www.youtube.com/watch?v=6orsmFndx_o



Notes: 

(1) Constraints are only applicable to line objects -- future improvements include implementation of constraints for arc objects. 

(2) Creation of composites and objects duplication are yet to be implemented. 
