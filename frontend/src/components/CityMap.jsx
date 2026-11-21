/*
    Display simulation visually on city map. 
    - Calculates object positions within grid cells (slotFor and sizeClass).
    - Creates and updates visual objects.
    - Animates objects as they move between positions (ease).
    - Draws and updates connections between objects.
    - Removes objects and connections that are no longer needed.
*/ 
import { useEffect, useLayoutEffect, useRef } from "react";
import { GRID } from "../utils/derive.js";

const SVG_NS = "http://www.w3.org/2000/svg";

// CALCULATES OBJECTS POSITIONS WITHIN GRID CELLS.
// Handles / calculates positions for each object, for different cases of sharing the grid space
function slotFor(count, index) {
    // Middle point
    if (count === 1) return [0, 0];
    // Side by side
    if (count === 2) return [[-0.22, 0], [0.22, 0]][index];
    // Triangular formation
    if (count === 3) return [[-0.22, -0.2], [0.22, -0.2], [0, 0.24]][index];
    // Sqaure formation
    if (count === 4) return [[-0.22, -0.22], [0.22, -0.22], [-0.22, 0.22], [0.22, 0.22]][index];
    // Calculate if more than 3, do rows of 3 objects 
    const cols = 3, rows = Math.ceil(count / cols); 
    return [((index % cols) - 1) * 0.3, (Math.floor(index / cols) - (rows - 1) / 2) * 0.3];
}

// Determines how large object should appear depending on how many share grid cell (css)
function sizeClass(count) {
    return count === 1 ? "" : count === 2 ? " s2" : count <= 4 ? " s4" : " s9";
}

// ANIMATION EASING FUNCTION 
const ease = progress => 
    (progress < 0.5 
        ? 2 * progress * progress 
        : 1 - Math.pow(-2 * progress + 2, 2) / 2);


// MAIN CITY MAP COMPONENTS 
// mapMarkers-> Map objects, routeLines-> Connections between objects, moveDurationMsRef-> Animation duration
export default function CityMap({ mapMarkers, routeLines, moveDurationMsRef }) {
    const mapRef = useRef(null); // Main map container
    const svgRef = useRef(null); // SVG container for lines
    const spritesRef = useRef(new Map()); // All vehicle/passenger HTML elements on map
    const lineElementsRef = useRef(new Map()); // All SVG line elements
    const routeLinesRef = useRef([]); // Latest line data / connections 

    // CONTINOUS ANIMATION LOOP
    // Updates object positions and connection lines every animation frame.
    useEffect(() => {
        let animationFrameId;

        // define function that runs on each animation frame, browser provides now timestamp
        function frame(now) {
            // update objects position 
            // loops through every sprite stored in sprites map
            // sprite holds the objects stored animation information 
            for (const sprite of spritesRef.current.values()) {
                // checks if object is currently being animated
                // if animationStartTime is null there is no animation , if it contains a timestamp it is moving toward a target
                if (sprite.animationStartTime !== null) {
                    const progress = Math.min(1, (now - sprite.animationStartTime) / moveDurationMsRef.current);
                    // pass animation through easing function 
                    const easedProgress = ease(progress);
                    // calculates objects current horizontal position
                    sprite.currentX = sprite.fromX + (sprite.targetX - sprite.fromX) * easedProgress;
                    // "" vertical position
                    sprite.currentY = sprite.fromY + (sprite.targetY - sprite.fromY) * easedProgress;
                    //check if animation has finished, reset time to null
                    if (progress >= 1) sprite.animationStartTime = null;
                }
                // updates object horizontal and vertical position on the map in the html 
                // grid coordinates -> percentage of map width / height
                sprite.element.style.left = (sprite.currentX / GRID * 100) + "%";
                sprite.element.style.top  = (sprite.currentY / GRID * 100) + "%";
            }

            // loops through current connections stored in routeLinesRef
            for (const line of routeLinesRef.current) {
                // find svg element belonging to the connection using its id 
                const lineElement = lineElementsRef.current.get(line.id);
                // if doesnt exist skip
                if (!lineElement) continue;
                // finds sprite representing the starting object of the connection
                // if the line connects vehcile to passenger for startSprite might represent a vehcile 
                const startSprite = spritesRef.current.get(line.startKey);
                // if starting object doesnt exist, hide connection line and move on to the next connection 
                if (!startSprite) { lineElement.style.visibility = "hidden"; continue; }
                // if starting object exists, show line
                lineElement.style.visibility = "visible";
                // starting and endining point of line, converting into precentages of the map
                const startXPercent = startSprite.currentX / GRID * 100, startYPercent = startSprite.currentY / GRID * 100;
                const endXPercent = line.endX / GRID * 100, endYPercent = line.endY / GRID * 100;
                // updates the SVG polyline's points, determining the shape of the connection 
                // {startXPercent}, {startYPercent} starting position, {endXPercent}, {startYPercent} horizontal movement towards the destination, {endXPercent}, {endYPercent} ending position. -> vehicles object moves in right angles not diagonal 
                lineElement.setAttribute("points", `${startXPercent},${startYPercent} ${endXPercent},${startYPercent} ${endXPercent},${endYPercent}`);
            }
            // schedule frame to run again on the next browser animation frame, continuing loop 
            animationFrameId = requestAnimationFrame(frame);
        }
        // starts animation loop for first time
        animationFrameId = requestAnimationFrame(frame);
        // cleanup function
        //  when the component unmounts, it cancels the next scheduled animation frame
        return () => cancelAnimationFrame(animationFrameId);
    }, []);

    // CREATE AND UPDATE MAP OBJECTS
    // Runs when mapMarkers or routeLines change.
    // Creates new elements, updates existing ones and removes outdated ones.
    useLayoutEffect(() => {
        // get the HTML map container using the reference created earlier 
        const mapElement = mapRef.current;
        // "" svg container
        const svgElement = svgRef.current;
        // Which objects should still exist on the map during current update
        const seenKeys = new Set();

        // Responsible for creating and updating an individual objects visual representation
        // marker - one map marker at time, x - target horizontal, y - target vertical, sizeCssClass - CSS class controlling the objects size 
        function place(marker, x, y, sizeCssClass) {
            // add objects unique key to seenKeys set, present in latest simulation data 
            seenKeys.add(marker.key);
            // Look inside sprites map to see if object already has visual representation 
            let sprite = spritesRef.current.get(marker.key);
            // if not already in sprites map, create it (holds the elements html, and its actual position)
            if (!sprite) {
                sprite = { element: document.createElement("div"), html: "", currentX: x, currentY: y,
                           fromX: x, fromY: y, targetX: x, targetY: y, animationStartTime: null };
                // controls elements vertical and horizontal position in the html element
                sprite.element.style.left = (x / GRID * 100) + "%";
                sprite.element.style.top  = (y / GRID * 100) + "%";
                spritesRef.current.set(marker.key, sprite);
                // add newly created html element to the map container 
                mapElement.appendChild(sprite.element);
            // if the object already exists check if its target position has changed 
            } else if (sprite.targetX !== x || sprite.targetY !== y) {
                // check if object should move or animate 
                if (marker.instant) {
                    sprite.currentX = sprite.targetX = sprite.fromX = x;
                    sprite.currentY = sprite.targetY = sprite.fromY = y;
                    sprite.animationStartTime = null;
                // If the object does not need to move instantly, it starts an animation
                } else {
                    // current horizontal/vertical position becomes the starting position 
                    // sets new target horizontal / vertical  
                    sprite.fromX = sprite.currentX; sprite.fromY = sprite.currentY;
                    sprite.targetX = x; sprite.targetY = y; 
                    sprite.animationStartTime = performance.now(); // Records the exact time the animation starts
                }
            }

            // combine css class 
            const className = "sprite " + marker.cssClass + sizeCssClass;
            // Checks whether the object's CSS classes have changed, if they have update them
            if (sprite.element.className !== className) sprite.element.className = className;
            // Check if objects html has changed, if it has update it, and store it in sprite.html
            if (sprite.html !== marker.html) { sprite.element.innerHTML = marker.html; sprite.html = marker.html;}
            // custom css property to the objects color
            sprite.element.style.setProperty("--c", marker.color);
            // updates title if necessary 
            if (sprite.element.title !== marker.title) sprite.element.title = marker.title;
        }

        // GROUP OBJECTS BY THEIR GRID CELL
        const markersByCell = new Map();
        // for each marker, in mapMarkers 
        mapMarkers.forEach(marker => {
            // creates string representing the objects grid cell 
            const cellKey = marker.gridX + "," + marker.gridY;
            // checks wether that map already has that grid cell, if not create an empty array for that cell
            if (!markersByCell.has(cellKey)) markersByCell.set(cellKey, []);
            // adds the current marker to the array belonging to its grid cell
            markersByCell.get(cellKey).push(marker);
        });

        // POSITION OBJECTS IN THEIR GRID CELL
        // loops through each group of objects in the markersByCell map 
        // aka each array containing all the objects that share one grid cell 
        for (const cellMarkers of markersByCell.values()) {
            // sorts by priorty, if its the same by its key 
            cellMarkers.sort((a, b) => a.sortPriority - b.sortPriority || (a.key < b.key ? -1 : a.key > b.key ? 1 : 0));
            // loops through each object in cellMarkers (marker is the current object, index is its index within the group)
            cellMarkers.forEach((marker, index) => {
                // function calculates its horizontal and vertial placement based off   
                const [offsetX, offsetY] = slotFor(cellMarkers.length, index);
                // create or update objects visual representation
                place(marker, marker.gridX + 0.5 + offsetX, marker.gridY + 0.5 + offsetY, sizeClass(cellMarkers.length));
            });
        }

        // REMOVE OBJECTS NO LONGER IN SIMULATION
        // list through every entry in the sprites map 
        for (const [key, sprite] of spritesRef.current) {
            // checks if object was not included in lastest mapMarkers data 
            if (!seenKeys.has(key)) { 
                sprite.element.remove();  // Remove its html element from the map
                spritesRef.current.delete(key);  // Remove its info from the sprites map too
            }
        }


        // CREATE AND UPDATE CONNECTION LINES 
        // update latest connection information 
        routeLinesRef.current = routeLines;
        // create set of all the lines that should currently exist 
        const wantedLineIds = new Set(routeLines.map(line => line.id));
        // loops through all the svg connection elements currently stored
        for (const [id, lineElement] of lineElementsRef.current) {
            // if id isnt shouldnt exist (its id isnt in wantedLineIds) 
            if (!wantedLineIds.has(id)) { 
                lineElement.remove(); // remove svg from set
                lineElementsRef.current.delete(id);  // remove from lineElementsRef map
            }
        }
        // loops through all the latest connections 
        routeLines.forEach(line => {
            // checks if svg element already exists
            let lineElement = lineElementsRef.current.get(line.id);
            // if it doesnt, create it
            if (!lineElement) {
                lineElement = document.createElementNS(SVG_NS, "polyline");
                svgElement.appendChild(lineElement); // add new polyline to svg container 
                lineElementsRef.current.set(line.id, lineElement); // stores polyline in lineElementsRef map, using its connection id, so we can find it again later and updat it  
            }
            //set lines color and class
            lineElement.setAttribute("class", line.cssClass);
            lineElement.setAttribute("stroke", line.color);
        });
    }, [mapMarkers, routeLines]);

    //MAP STRUCTURE AND GRID LABELS
    return (
        <div id="cityMap" ref={mapRef}>
            <svg id="links" viewBox="0 0 100 100" preserveAspectRatio="none" ref={svgRef} />
            {Array.from({ length: GRID }, (_, i) => (
                <span key={"x" + i} className="axis ax"
                      style={{ left: (i + 0.5) * 100 / GRID + "%", top: "1.8%" }}>{i}</span>
            ))}
            {Array.from({ length: GRID }, (_, i) => (
                <span key={"y" + i} className="axis ay"
                      style={{ left: "1.5%", top: (i + 0.5) * 100 / GRID + "%" }}>{i}</span>
            ))}
        </div>
    );
}