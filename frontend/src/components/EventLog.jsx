/*
    Displays log of events happening in simulation.

    Receive event details from Java -> display event log 
*/
import { useLayoutEffect, useRef } from "react";

// colors are set inline so no stylesheet rule can override them
const rowStyle = vehicle => vehicle ? {
    "--c": vehicle.color,
    borderLeft: `4px solid ${vehicle.color}`,
    background: `color-mix(in srgb, ${vehicle.color} 14%, transparent)`
} : undefined;
const tagStyle = vehicle => vehicle ? { background: vehicle.color, color: "var(--on-color)" } : undefined;

// the component: receives the events (newest first) and the dashboard data
export default function EventLog({ events, dashboard }) {
    const listRef = useRef(null);    // points to the <ul> element on the page
    const newestId = events.length ? events[0].id : null;

    // scroll back to the top when a new event arrives
    useLayoutEffect(() => {
        if (listRef.current) listRef.current.scrollTop = 0;
    }, [newestId]);

    return (
        <ul id="eventLog" ref={listRef}>
            {events.length === 0 && <li className="empty">Waiting for requests...</li>}
            {events.map(event => {
                if (event.type === "battery") {
                    return (
                        <li key={event.id} className="ev battery new">
                            <span className="tag battery">BATTERY</span>
                            <b>{event.vehicleType} {event.vehicleId}</b> battery reduced after ride. Current battery level: {event.batteryLevel}%
                        </li>
                    );
                }
                // find the assigned vehicle to get its code, icon, and color
                const vehicle = dashboard?.fleet.find(candidate =>
                    candidate.type === event.vehicleType && candidate.id === event.vehicleId);
                return (
                    <li key={event.id}
                        className={"ev request new" + (vehicle ? " assigned" : "")}
                        style={rowStyle(vehicle)}>
                        <span className={"tag " + (event.isShared ? "shared" : "request")} style={tagStyle(vehicle)}>
                            {event.isShared ? "SHARED REQUEST" : "REQUEST"}
                        </span>
                        <b>{dashboard?.nameOf(event.userId)}</b> requests a <b>{event.service}</b> service from <b>{event.from}</b> to <b>{event.to}</b>
                        {vehicle && (
                            <span className="outcome" style={{ color: vehicle.color }}>
                                → ASSIGNED TO <span className="vehicle-icon">{vehicle.icon}</span> {vehicle.code}
                            </span>
                        )}
                    </li>
                );
            })}
        </ul>
    );
}