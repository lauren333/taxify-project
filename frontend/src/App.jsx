import { useEffect, useMemo, useRef, useState } from "react";
import { deriveDashboard } from "./utils/derive.js";
import { useSimulation } from "./utils/useSimulation.js";
import CityMap from "./components/CityMap.jsx";
import EventLog from "./components/EventLog.jsx";
import { Play, Pause, SkipForward } from 'lucide-react';

const EMPTY = [];
// How long to make dropoff status remain visible breify, after the simulation has moved on 
const DROPOFF_HOLD_MS = 1000;

// helper to let vehicle stats fit in the vehicle tiles for long strings
function FitText({ children }) {
    const ref = useRef(null);
    const [fontSize, setFontSize] = useState(10);

    useEffect(() => {
        const element = ref.current;
        if (!element) return;

        let size = 10;
        element.style.fontSize = `${size}px`;

        while (
            size > 6 &&
            (element.scrollHeight > element.clientHeight ||
             element.scrollWidth > element.clientWidth)
        ) {
            size -= 0.5;
            element.style.fontSize = `${size}px`;
        }

        setFontSize(size);
    }, [children]);

    return (
        <span
            ref={ref}
            className="vinfo"
            style={{ fontSize: `${fontSize}px` }}
        >
            {children}
        </span>
    );
}

// Helper that keeps dropoff notification on screen for longer 
function useDropoffHold(dashboard) {
    // useRef: preserve previous vehicle state for comparison (avoiding unnecessary rerender)    
    const previousVehiclesRef = useRef(new Map());
    // Stores the dropoff notification and function to update it 
    const [recentDropoffs, setRecentDropoffs] = useState({});

    // Everytime simulation steps (dashboard changes) -> checks for newly dropped off passengers 
    useEffect(() => {
        // New map for current state of BUSY vehicles 
        const busyVehiclesByKey = new Map();
        (dashboard ? dashboard.fleet : []).forEach(vehicle => { if (vehicle.busy) busyVehiclesByKey.set(vehicle.key, vehicle); });
        
        // New dictionary of vehicles which just had a drop off, include which passenger id got off
        const newDropoffs = {};
        // goes through previous snapshot 
        previousVehiclesRef.current.forEach((previousVehicle, key) => {
            // Current vehicle info for given key 
            const currentVehicle = busyVehiclesByKey.get(key);
            // Set of passenger ids currently in that vehicle
            const currentRiderIds = new Set(currentVehicle ? currentVehicle.riders.map(rider => rider.userId) : []);
            // Users who were there but not anymore
            const droppedUserIds = previousVehicle.riders
                .filter(rider => rider.aboard && !currentRiderIds.has(rider.userId))
                .map(rider => rider.userId);
            // If at least one passenger detected at drop off, share that vehicles id as a key and print user has been dropped off
            if (droppedUserIds.length) newDropoffs[key] = { vehicle: previousVehicle, droppedUserIds };
        });
        // replaces old vehicle snapshot to new 
        previousVehiclesRef.current = busyVehiclesByKey;

        // returns array of its property names, vehicles ids that have had a dropoff 
        const vehicleKeys = Object.keys(newDropoffs);
        // check if greater than 0, then run react state update 
        if (vehicleKeys.length) {
            // update recentDropoffs state with new information 
            setRecentDropoffs(current => ({ ...current, ...newDropoffs }));
            // Delay removal of dropoff message for one second 
            setTimeout(() => setRecentDropoffs(current => {
                const remaining = { ...current };
                // only clear it if a newer drop-off hasn't replaced it
                vehicleKeys.forEach(key => { if (remaining[key] === newDropoffs[key]) delete remaining[key]; });
                return remaining;
            }), DROPOFF_HOLD_MS);
        }
    }, [dashboard]);
    return recentDropoffs;
}

// vehile tiles pn dashboard with updates 
function VehicleGrid({ dashboard }) {
    const recentDropoffs = useDropoffHold(dashboard);

    if (!dashboard) {
        return <div id="vehicleGrid" className="vgrid"><span>Waiting for Java simulation...</span></div>;
    }

    const { nameOf } = dashboard;
    const joinNames = riders => riders.map(rider => nameOf(rider.userId)).join(" & ");
    const formatLocations = locations => locations.map(location => `(${location.x}, ${location.y})`).join(" & ");

    return (
        <div id="vehicleGrid" className="vgrid">
            {dashboard.fleet.map(vehicle => {
                const isMicro = vehicle.kind === "micro";
                const recentDropoff = recentDropoffs[vehicle.key];
                let tileClass, stage, info = "—", detail;

                if (recentDropoff) {
                    tileClass = "dropoff";
                    stage = "Drop-off";
                    const dropoffSpots = recentDropoff.vehicle.dropoffs.filter(dropoff => recentDropoff.droppedUserIds.includes(dropoff.userId));
                    const droppedNames = recentDropoff.droppedUserIds.map(userId => nameOf(userId)).join(" & ");
                    info = `${droppedNames} ✓` + (dropoffSpots.length ? ` ${formatLocations(dropoffSpots)}` : "");
                    detail = `${vehicle.code} is dropping off ${droppedNames}` + (dropoffSpots.length ? ` at ${formatLocations(dropoffSpots)}` : "");

                } else if (!vehicle.busy) {
                    tileClass = "idle";
                    stage = vehicle.available ? "Idle" : "Battery";
                    detail = `${vehicle.code} · ${dashboard.stateLabel(vehicle)}`;

                } else if (vehicle.ridersWaiting.length) {
                    tileClass = "reserved";
                    stage = "Reserved";
                    const nextRiderName = joinNames(vehicle.ridersWaiting.slice(0, 1));
                    info = isMicro ? `${nextRiderName} walking to ${vehicle.code} (${vehicle.x}, ${vehicle.y})` : `${nextRiderName} (${vehicle.nextStopX}, ${vehicle.nextStopY})`;
                    detail = isMicro
                        ? `${nextRiderName} is walking to ${vehicle.code} (${vehicle.nextStopX}, ${vehicle.nextStopY})`
                        : vehicle.ridersAboard.length
                            ? `${vehicle.code} is driving ${joinNames(vehicle.ridersAboard)} · now driving to ${nextRiderName} at (${vehicle.nextStopX}, ${vehicle.nextStopY})`
                            : `${vehicle.code} is driving to ${nextRiderName} at (${vehicle.nextStopX}, ${vehicle.nextStopY})`;
                    if (vehicle.ridersWaiting.length > 1) detail += ` · then ${joinNames(vehicle.ridersWaiting.slice(1))}`;
                } else {
                    tileClass = "service";
                    stage = "In service";
                    info = vehicle.ridersAboard.map(rider => {
                        const riderDropoff = vehicle.dropoffs.find(dropoff => dropoff.userId === rider.userId);
                    
                        return `${nameOf(rider.userId)}${riderDropoff ? ` → (${riderDropoff.x}, ${riderDropoff.y})` : ""}`;
                    }).join(" · ");
                    const aboardDropoffs = vehicle.dropoffs.filter(dropoff => vehicle.ridersAboard.some(rider => rider.userId === dropoff.userId));
                    detail = `${vehicle.code} is ${isMicro ? "riding" : "driving"} ${joinNames(vehicle.ridersAboard)}`
                        + (aboardDropoffs.length ? ` to ${formatLocations(aboardDropoffs)}` : "");
                }

                return (
                    <div key={vehicle.key} className={"vtile " + tileClass} style={{ "--c": vehicle.color }} title={detail}>
                        <span className="vcode"><span className="vehicle-icon">{vehicle.icon}</span> {vehicle.code}</span>
                        <span className="vstage">{stage}</span>
                        <FitText>{info}</FitText>
                    </div>
                );
            })}
        </div>
    );
}

function FleetTable({ dashboard }) {
    const recentDropoffs = useDropoffHold(dashboard);
    return (
        <tbody id="vehicleTable">
            {!dashboard ? <tr><td colSpan={11}>Waiting for Java simulation...</td></tr> : dashboard.fleet.map(vehicle => {
                const passengerNames = vehicle.riders.map(rider => dashboard.nameOf(rider.userId)).join(", ");
                const batteryText = vehicle.kind === "micro" ? ` · 🔋${Math.round(vehicle.batteryLevel)}%` : "";
                return (
                    <tr key={vehicle.key}>
                        <td>
                            <span className="dotc" style={{ "--c": vehicle.busy ? vehicle.color : "#4a5675" }}></span>
                            {vehicle.icon} {vehicle.code}{batteryText}
                        </td>
                        <td>{vehicle.type}</td>
                        <td>({vehicle.x}, {vehicle.y})</td>
                        <td>
                            <span className={"pill " + (recentDropoffs[vehicle.key] ? "dropoff" : vehicle.phase)}>
                                {recentDropoffs[vehicle.key] ? "Dropping off" : dashboard.stateLabel(vehicle)}
                            </span>
                        </td>
                        <td>{passengerNames || "--"}</td>
                        <td>{vehicle.dropoffs.length ? vehicle.dropoffs.map(dropoff => `(${dropoff.x}, ${dropoff.y})`).join(", ") : "--"}</td>
                        <td>{vehicle.serviceCount ?? 0}</td>
                        <td>{vehicle.distanceKm ?? 0} km</td>
                        <td>€{(vehicle.revenue ?? 0).toFixed(2)}</td>
                        <td>{vehicle.reviewCount ?? 0}</td>
                        <td>{vehicle.reviewCount > 0 ? `${vehicle.rating.toFixed(2)} ★` : "--"}</td>
                    </tr>
                );
            })}
        </tbody>
    );
}

function PassengerTable({ dashboard }) {
    return (
        <tbody id="userTable">
            {!dashboard ? <tr><td colSpan={4}>Waiting for Java simulation...</td></tr> : dashboard.users.map(user => {
                const assignment = dashboard.assignmentByUserId.get(user.id);
                let status = "Idle";
                if (assignment) {
                    const isMicro = assignment.vehicle.kind === "micro";
                    status = assignment.aboard
                        ? (isMicro ? `On ${assignment.vehicle.code}` : `Riding ${assignment.vehicle.code}`)
                        : (isMicro ? `Walking to ${assignment.vehicle.code}` : `Waiting for ${assignment.vehicle.code}`);
                }
                return (
                    <tr key={user.id}>
                        <td>{user.id}</td>
                        <td>{user.name}</td>
                        <td>({user.x}, {user.y})</td>
                        <td>{status}</td>
                    </tr>
                );
            })}
        </tbody>
    );
}

export default function App() {
    const simulation = useSimulation();
    const { snapshot } = simulation;
    const [showIdleVehicles, setShowIdleVehicles] = useState(true);
    const [showIdleUsers, setShowIdleUsers] = useState(true);
    const [isRunning, setIsRunning] = useState(false);

    const dashboard = useMemo(
        () => (snapshot ? deriveDashboard(snapshot, showIdleVehicles, showIdleUsers) : null),
        [snapshot, showIdleVehicles, showIdleUsers]
    );

    useEffect(() => {
        if (snapshot) setIsRunning(snapshot.running);
    }, [snapshot?.running]);

    return (
        <>
            <aside className="sidebar">
                <h1>TAXIFY</h1>
                <p className="subtitle">SMART MOBILITY</p>
                <nav>
                    <a className="active" href="#dashboard">Dashboard</a>
                    <a href="#live">Live Map</a>
                    <a href="#fleet">Vehicle Fleet</a>
                    <a href="#passengers">Passengers</a>
                </nav>
                <div className="connection">
                    <span className="dot"></span>
                    Java Simulation
                    <small>{simulation.connectionStatus}</small>
                </div>
            </aside>
            <main>
                <header id="dashboard">
                    <div>
                        <h2>Dashboard</h2>
                        <p>Welcome to your Taxify management centre.</p>
                    </div>
                    <span className="date">FLEET MANAGEMENT</span>
                </header>
                <section className="stats">
                    <div className="card">
                        <p>Total Vehicles</p>
                        <h2>{dashboard ? dashboard.fleet.length : "--"}</h2>
                        <small>Across the fleet</small>
                    </div>
                    <div className="card">
                        <p>Available Vehicles</p>
                        <h2>{dashboard ? dashboard.fleet.filter(vehicle => vehicle.phase === "idle" && vehicle.available).length : "--"}</h2>
                        <small>Idle, ready for service</small>
                    </div>
                    <div className="card">
                        <p>Active Services</p>
                        <h2>{snapshot ? snapshot.totalServices : 0}</h2>
                        <small>Services in progress</small>
                    </div>
                    <div className="card">
                        <p>Shared Rides</p>
                        <h2>{dashboard ? dashboard.fleet.filter(vehicle => vehicle.isShared).length : 0}</h2>
                        <small>Currently shared</small>
                    </div>
                </section>
                <section className="panel" id="live">
                    <div className="panel-header">
                        <div>
                            <h3>Live City Map</h3>
                            <p>Steps <strong>{snapshot ? snapshot.tick : 0}</strong> · <span>{snapshot && snapshot.running ? "running" : "paused"}</span></p>
                        </div>
                        <div className="controls">
                            <div className="simulation-controls">
                                <button
                                    onClick={() => {
                                        if (isRunning) {
                                            setIsRunning(false);
                                            simulation.pauseSimulation();
                                        } else {
                                            setIsRunning(true);
                                            simulation.startSimulation();
                                        }
                                    }}
                                >
                                    {isRunning ? (
                                        <><Pause size={15} /> Pause</>
                                    ) : (
                                        <><Play size={15} /> Start</>
                                    )}
                                </button>
                                <button onClick={simulation.nextStep}>
                                    <SkipForward size={15} /> Next Step
                                </button>
                            </div>                            
                            <select id="speed" value={simulation.stepIntervalMs} onChange={event => simulation.changeSpeed(parseInt(event.target.value, 10))}>
                                <option value="2000">Very slow</option>
                                <option value="1500">Slow</option>
                                <option value="1000">Normal</option>
                                <option value="500">Fast</option>
                                <option value="250">Very fast</option>
                                <option value="120">Turbo</option>
                            </select>
                            <label className="toggle">
                                <input type="checkbox" checked={showIdleVehicles}
                                       onChange={event => setShowIdleVehicles(event.target.checked)} /> idle vehicles
                            </label>
                            <label className="toggle">
                                <input type="checkbox" checked={showIdleUsers}
                                       onChange={event => setShowIdleUsers(event.target.checked)} /> idle users
                            </label>
                        </div>
                    </div>
                    <div className="live-layout">
                        <div className="map-section">
                            <CityMap
                                mapMarkers={dashboard ? dashboard.mapMarkers : EMPTY}
                                routeLines={dashboard ? dashboard.routeLines : EMPTY}
                                moveDurationMsRef={simulation.moveDurationMsRef}
                            />
                            <div className="legend">
                                <div className="legend-column">
                                    <div><span className="lg idle"></span> Faded = idle</div>
                                    <div><span className="lg dotted"></span> = heading to a pickup</div>
                                    <div><span className="lg solid"></span> = heading to a drop-off</div>
                                </div>
                                <div className="legend-column">
                                    <div>🏁 = vehicle drop-off destination</div>
                                    <div>🧍 On a vehicle = rider aboard</div>
                                    <div>👥 = two riders aboard</div>
                                </div>
                                <div className="legend-column">
                                    <div>Pulsing person = waiting for vehicle</div>
                                    <div>🚕 <b>T</b> Taxi · 🚐 <b>S</b> Shuttle · 🛴 <b>Sc</b> Scooter · 🚲 <b>B</b> Bike</div>
                                    <div>Dashed orange ring = scooter/bike unavailable (battery)</div>
                                </div>
                            </div>
                        </div>
                        <div className="side">
                            <h4>Vehicle Updates</h4>
                            <VehicleGrid dashboard={dashboard} />

                            <h4>Live Taxify Updates</h4>
                            <EventLog
                                events={snapshot ? snapshot.events : EMPTY}
                                dashboard={dashboard}
                            />
                        </div>
                    </div>              
                </section>
                <section className="panel" id="fleet">
                    <div className="panel-header">
                        <div>
                            <h3>Vehicle Fleet</h3>
                            <p>Monitor your vehicles</p>
                        </div>
                        <button onClick={simulation.refresh}>Refresh</button>
                    </div>
                    <table>
                    <thead>
                        <tr>
                            <th>Vehicle</th>
                            <th>Type</th>
                            <th>Location</th>
                            <th>State</th>
                            <th>Passengers</th>
                            <th>Destination</th>
                            <th>Services</th>
                            <th>Distance</th>
                            <th>Revenue</th>
                            <th>Reviews</th>
                            <th>Rating</th>
                        </tr>
                    </thead>
                        <FleetTable dashboard={dashboard} />
                    </table>  
                </section>
                <section className="panel" id="passengers">
                    <h3>Passengers</h3>
                    <table>
                        <thead>
                            <tr>
                                <th>User</th>
                                <th>Name</th>
                                <th>Location</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <PassengerTable dashboard={dashboard} />
                    </table>
                </section>
            </main>
        </>
    );
}