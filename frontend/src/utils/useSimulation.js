/*
    Retrieves the information from backend and manages simulation updates. 
    - Backend Communication 
    - Position Updates helpers (withNewPositions) 
    - Simulation State and Refrences (React State and Refs)
    - Animation and Timing Management
    - Snapshot Management (applySnapshot and finishTransition)
    - Backround Refresh and Polling
    - Simulation Controls
    - Return Simulation Data  
*/

import { useEffect, useRef, useState } from "react";
import { flushSync } from "react-dom";

// define backend api path 
const API = "/api";

// --- BACKEND COMMUNICATION ---
// function to communicate with backend 
async function call(path) {
    const response = await fetch(API + path);
    return response.json();
}

// --- POSITION UPDATE HELPERS	---
// Creates a new state based on the previous state, updating only positions. 
function withNewPositions(baseSnapshot, freshSnapshot) {
    // function that converts array into a map indexed by ids 
    const mapById = list => new Map(list.map(object => [object.id, object]));
    // create three lookup maps (vehicles, microvehicles, users)
    const freshVehiclesById = mapById(freshSnapshot.vehicles),
          freshMicroVehiclesById = mapById(freshSnapshot.microVehicles),
          freshUsersById = mapById(freshSnapshot.users);
    // oldObject is the old object, freshById is a map containing fresh objects 
    // finds matching object via ids, creates new objects with old properties BUT updated location 
    const move = (oldObject, freshById) => {
        const freshObject = freshById.get(oldObject.id);
        return freshObject ? { ...oldObject, x: freshObject.x, y: freshObject.y } : oldObject;
    };
    return {
        ...baseSnapshot,
        vehicles:      baseSnapshot.vehicles.map(object => move(object, freshVehiclesById)),
        microVehicles: baseSnapshot.microVehicles.map(object => move(object, freshMicroVehiclesById)),
        users:         baseSnapshot.users.map(object => move(object, freshUsersById))
    };
}

// --- SIMULATION STATE AND REFERENCES ---
export function useSimulation() {
    const [snapshot, setSnapshot] = useState(null);
    const [stepIntervalMs, setStepIntervalMs] = useState(1000);
    const [connectionStatus, setConnectionStatus] = useState("Connecting...");

    // Stores values that persist between renders without triggering a re-render
    const stepIntervalMsRef = useRef(1000);
    const moveDurationMsRef = useRef(450);
    const shownSnapshotRef = useRef(null); // The snapshot currently used as the starting point for the displayed movement
    const latestSnapshotRef = useRef(null); // The most recently received snapshot
    const transitionTimerRef = useRef(null); // Keeps track of whether a movement animation is still in progress
    const isPollingRef = useRef(false);

    // --- ANIMATION AND TIMING MANAGEMENT	---
    // Control animation duration 
    function updateMoveDuration() {
        moveDurationMsRef.current = Math.max(60, Math.min(450, stepIntervalMsRef.current * 0.45));
    }

    // Synchronises the displayed snapshot with the latest received snapshot 
    function finishTransition() {
        transitionTimerRef.current = null; // Clears stored timer
        shownSnapshotRef.current = latestSnapshotRef.current; // Updates currently displayed snapshot   
        setSnapshot(latestSnapshotRef.current); // Update react state with latest snapshot 
    }

    // SNAPSHOT MANAGEMENT	
    // Processes a new simulation snapshot received from the backend
    function applySnapshot(newSnapshot) {
        // If no previous snapshot, this is the first update 
        if (!latestSnapshotRef.current) {
            shownSnapshotRef.current = latestSnapshotRef.current = newSnapshot;
            setSnapshot(newSnapshot);
            return;
        }

        // If new snapshot has the same simulation tick as last one
        if (newSnapshot.tick === latestSnapshotRef.current.tick) {
            // update latest snapshot
            latestSnapshotRef.current = newSnapshot;
            // if no animation is currently in progress, update displayed snapshot immediately 
            if (!transitionTimerRef.current) {
                shownSnapshotRef.current = newSnapshot;
                setSnapshot(newSnapshot);
            }
            // otherwise leave the current animation alone
            return;
        }

        // Handles if simulation tick arrives while an earlier transition is being animated 
        if (transitionTimerRef.current) {
            clearTimeout(transitionTimerRef.current); //cancels exisiting timer
            flushSync(finishTransition); // synchronously processes the React state update  aka finishes first transition then processes the second
        }
        // stores new snapshot
        latestSnapshotRef.current = newSnapshot;
        // take currently displayed snpashot and update the its coordinates using the new snapshot
        setSnapshot(withNewPositions(shownSnapshotRef.current, newSnapshot));
        // Schedules the transition to finish after the vehicle movement animation 
        transitionTimerRef.current = setTimeout(finishTransition, moveDurationMsRef.current + 30);
    }

    // --- BACKEND REFRESH AND POLLING	---
    // function that retrieves the latest state from the backend
    async function refresh() {
        // prevents overlapping polling requests
        if (isPollingRef.current) return;
        isPollingRef.current = true;
        // Requests the latest state from /api/state.
        try {
            applySnapshot(await call("/state"));
            setConnectionStatus("Connected");
        } catch (error) {
            setConnectionStatus("Disconnected");
            console.error("Could not connect to Taxify:", error);
        // This runs whether the request succeeded or failed. allows the next request.
        } finally {
            isPollingRef.current = false;
        }
    }

    // Automatically requests updated simulation data at regular intervals (polling backend)
    // runs once when component mounts
    useEffect(() => {
        // controls if simulation should stop, stores  the next polling timeout
        let stopped = false, pollTimer;
        async function pollLoop() {
            await refresh(); // get last simulation data 
            if (stopped) return; 
            const isRunning = latestSnapshotRef.current && latestSnapshotRef.current.running;  // check is simulationn is running 
            const delay = isRunning ? Math.max(60, Math.min(200, stepIntervalMsRef.current / 3)) : 400; // determines how long to wait before polling again
            pollTimer = setTimeout(pollLoop, delay); //schedule next polling cycle 
        }
        pollLoop(); // starts the loop
        return () => { stopped = true; clearTimeout(pollTimer); clearTimeout(transitionTimerRef.current); }; // Stops polling and clears pending timers when the component unmounts
    }, []);

    // calc speed, starts somulation on backend, updates dashboard
    async function startSimulation() { updateMoveDuration(); await call("/start?ms=" + stepIntervalMsRef.current); refresh(); }
    // pauses simulation, refresh dashboard 
    async function pauseSimulation() { await call("/pause"); refresh(); }
    // Calculates simulation speed, advances simulation one step 
    async function nextStep()        { updateMoveDuration(); await call("/step"); refresh(); }

    // --- SIMULATION CONTROLS ---
    // if already running set speed and stop and restart simulation
    async function changeSpeed(intervalMs) {
        stepIntervalMsRef.current = intervalMs;
        setStepIntervalMs(intervalMs);
        updateMoveDuration();
        const status = await call("/status");
        if (status.running) startSimulation();
    }

    // --- RETURN SIMULATION DATA --- 
    return {
        snapshot, stepIntervalMs, connectionStatus, moveDurationMsRef,
        startSimulation, pauseSimulation, nextStep, changeSpeed, refresh
    };
}