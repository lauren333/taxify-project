/* 
    Tranform and prepare raw simulation data for dashboard to display.
    -  Passenger Info (users + helper nameOf) 
    -  Vehicle Info (fleet)
    -  Passenger-Vehicle Assignments (assignmentByUserId + helper stateLabel) 
    -  Map Objects (mapMarkers)
    -  Vehicle Movement Lines (routeLines), 
 */

export const GRID = 10;
// javascript object, stores key-value pairs
const ICONS = { Taxi: "🚕", Shuttle: "🚐", ElectricScooter: "🛴", ElectricBike: "🚲" };
const CODES = { Taxi: "T", Shuttle: "S", ElectricScooter: "Sc", ElectricBike: "B" };

// prevent security problems 
export function escapeHtml(text) {
    return String(text).replace(/[&<>"]/g, character =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[character]));
}

// Takes vehicles index, vehicleIndex, and returns a unique hue / color for the vehicle 
const colorFor = vehicleIndex => `hsl(${Math.round((vehicleIndex * 137.508) % 360)} 80% 62%)`;

// snapshot is the raw simulation data, showIdleVehicles + showIdleUsers are boolean values
export function deriveDashboard(snapshot, showIdleVehicles, showIdleUsers) {
    // stores everything needed to appear on the map
    const mapMarkers = [];

    // --- PASSENGER INFORMATION --- 
    // create key, value pairs -> where the value is the user object (user-id,user-name) 
    const userById = new Map(snapshot.users.map(user => [user.id, user]));
    // returns users name 
    const nameOf = userId => (userById.has(userId) ? userById.get(userId).name : "user " + userId);
    // Combine names into one string 
    const plainNames = riders => riders.map(rider => nameOf(rider.userId)).join(", ");

    // --- VEHICLE FLEET --- 
    // build fleet of vehicles  
    // The ...vehicle is the spread operator. It copies all properties of the original vehicle. Then adds: kind 
    const fleet = [
        ...snapshot.vehicles.map(vehicle => ({ ...vehicle, kind: "vehicle" })),
        ...snapshot.microVehicles.map(vehicle => ({ ...vehicle, kind: "micro" }))
    ];

    // for each vehcile we prepare its information for web side where vehicle is current vehicle, vehicleIndex is current index
    fleet.forEach((vehicle, vehicleIndex) => {
        vehicle.key = `v-${vehicle.type}-${vehicle.id}`; //unique identifier 
        vehicle.code = (CODES[vehicle.type] || "?") + vehicle.id; // gets short code + id 
        vehicle.icon = ICONS[vehicle.type] || "🚗"; // gets icon emoji for vehicle type 
        vehicle.color = colorFor(vehicleIndex);
        vehicle.riders = vehicle.riders || [];
        vehicle.dropoffs = vehicle.dropoffs || [];
        vehicle.ridersAboard = vehicle.riders.filter(rider => rider.aboard); // filters only onboard users
        vehicle.ridersWaiting = vehicle.riders.filter(rider => !rider.aboard); // filters passengers waiting to enter 
        vehicle.phase = !vehicle.busy ? "idle" : (vehicle.ridersAboard.length ? "ride" : "pickup"); // determine the vehicles state 
    });

    // --- PASSENGER-VEHICLE ASSIGNMENT --- 
    // map contains rider id as key, and for value the vehicle, and if on board or not
    const assignmentByUserId = new Map();
    fleet.forEach(vehicle => vehicle.riders.forEach(rider =>
        assignmentByUserId.set(rider.userId, { vehicle, aboard: rider.aboard })));

    const stateLabel = vehicle => {
        if (vehicle.phase === "pickup") return vehicle.kind === "micro" ? "Reserved" : "Picking up";
        if (vehicle.phase === "ride") {
            if (vehicle.kind === "micro") return "In use";
            return vehicle.ridersWaiting.length ? "On board, picking up 2nd" : "On board";
        }
        return vehicle.available ? "Idle" : "Unavailable (battery)";
    };

    
    // --- MAP OBJECTS: destination ---  
    fleet.forEach(vehicle => {
        if (!vehicle.busy) return; // if vehicle is not busy, skip  
        // loops through all destinations associated with a vehicle  
        vehicle.dropoffs.forEach(dropoff => {
            const userName = nameOf(dropoff.userId);
            mapMarkers.push({
                key: `d-${vehicle.key}-${dropoff.userId}`,
                cssClass: "dest" + (vehicle.phase === "pickup" ? " ghost" : ""), //if picking up user still add CSS ghost class
                html: `<div class="disc">🏁</div><span class="chip">→${vehicle.code}${vehicle.dropoffs.length > 1 ? " " + escapeHtml(userName) : ""}</span>`,
                color: vehicle.color,
                title: `Drop-off for ${userName} (${vehicle.code}) at (${dropoff.x}, ${dropoff.y})`,
                gridX: dropoff.x, gridY: dropoff.y, instant: true, sortPriority: 4
            });
        });
    });

    //  --- MAP OBJECTS: vehicle ---   
    fleet.forEach(vehicle => {
        if (vehicle.phase === "idle" && !showIdleVehicles) return;

        let passengerBadge = "";
        if (vehicle.ridersAboard.length) passengerBadge = vehicle.ridersAboard.length > 1 ? "👥" : "🧍";

        let title = `${vehicle.type} ${vehicle.id} · ${stateLabel(vehicle)}`;
        if (vehicle.ridersAboard.length) title += ` · aboard: ${plainNames(vehicle.ridersAboard)}`;
        if (vehicle.ridersWaiting.length) title += ` · ${vehicle.kind === "micro" ? "reserved by" : "picking up"}: ${plainNames(vehicle.ridersWaiting)}`;
        if (vehicle.kind === "micro") title += ` · battery ${Math.round(vehicle.batteryLevel)}%`;

        mapMarkers.push({
            key: vehicle.key,
            cssClass: `vehicle ${vehicle.phase}` + (vehicle.phase === "idle" && !vehicle.available ? " low" : ""),
            html: `<div class="disc">${vehicle.icon}</div><span class="chip">${vehicle.code}</span>` +
                  (passengerBadge ? `<span class="pax">${passengerBadge}</span>` : ""),
            color: vehicle.color, title,
            gridX: vehicle.x, gridY: vehicle.y, instant: false, sortPriority: vehicle.phase === "idle" ? 1 : 0
        });
    });

    // --- MAP OBJECTS: users ---  
    snapshot.users.forEach(user => {
        // check wether user is assigned vehicle 
        const assignment = assignmentByUserId.get(user.id);
        // if already assigned and onboard dont show seperate user icon 
        if (assignment && assignment.aboard) return;
        // if waiting show icon 
        if (assignment) {
            const verb = assignment.vehicle.kind === "micro" ? "walking to" : "waiting for";
            mapMarkers.push({
                key: `u-${user.id}`, cssClass: "user assigned waiting",
                html: `<div class="disc">🧍</div><span class="chip">${escapeHtml(user.name)} · ${assignment.vehicle.code}</span>`,
                color: assignment.vehicle.color, title: `${user.name} (user ${user.id}) · ${verb} ${assignment.vehicle.code}`,
                gridX: user.x, gridY: user.y, instant: false, sortPriority: 2
            });
        // if idle show greyed out icon 
        } else if (showIdleUsers) {
            mapMarkers.push({
                key: `u-${user.id}`, cssClass: "user idle",
                html: `<div class="disc"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="3.5"/><path d="M5 21v-2a7 7 0 0 1 14 0v2"/></svg></div><span class="chip">${escapeHtml(user.name)}</span>`,
                color: "#4a5675", title: `${user.name} (user ${user.id}) · idle`,
                gridX: user.x, gridY: user.y, instant: false, sortPriority: 3
            });
        }
    });

    // --- VEHICLE MOVEMENT LINES --- 
    // connect busy vehicles to their destination points 
    const routeLines = [];
    fleet.forEach(vehicle => {
        if (!vehicle.busy) return;
        // pickup vs. ride flag for css (pulses when in pickup)
        if (vehicle.kind === "micro") {
            // connects passenger to vehicles location
            if (vehicle.phase === "pickup" && vehicle.riders[0]) {
                routeLines.push({ id: `l-${vehicle.key}`, startKey: `u-${vehicle.riders[0].userId}`,
                                  endX: vehicle.x + 0.5, endY: vehicle.y + 0.5, color: vehicle.color, cssClass: "pickup" });
            // if in service connect vehicle to target locaiton
            } else if (vehicle.nextStopX !== undefined) { 
                routeLines.push({ id: `l-${vehicle.key}`, startKey: vehicle.key,
                                  endX: vehicle.nextStopX + 0.5, endY: vehicle.nextStopY + 0.5, color: vehicle.color, cssClass: "ride" });
            }
        } else if (vehicle.nextStopX !== undefined) {
            routeLines.push({ id: `l-${vehicle.key}`, startKey: vehicle.key,
                              endX: vehicle.nextStopX + 0.5, endY: vehicle.nextStopY + 0.5, color: vehicle.color,
                              cssClass: vehicle.heading === "pickup" ? "pickup" : "ride" });
        }
    });
    return { fleet, users: snapshot.users, mapMarkers, routeLines, assignmentByUserId, nameOf, stateLabel };
}