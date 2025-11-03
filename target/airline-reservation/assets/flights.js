// assets/flights.js

document.addEventListener('DOMContentLoaded', function () {
    function renderFlights(flights) {
        const tbody = document.getElementById('flightsList');
        tbody.innerHTML = flights.length === 0 ? `<tr><td colspan="7" class="text-center">Không có dữ liệu</td></tr>` :
        flights.map(flight => `
            <tr>
                <td>${flight.flightNumber}</td>
                <td>${flight.route ? flight.route.origin + ' → ' + flight.route.destination : ''}</td>
                <td>${new Date(flight.departureTime).toLocaleString()}</td>
                <td>${new Date(flight.arrivalTime).toLocaleString()}</td>
                <td>${flight.price.toLocaleString()}</td>
                <td>${flight.availableSeats}</td>
                <td><a href="/flights/${flight.id}" class="btn btn-sm btn-info">Chi tiết</a></td>
            </tr>`).join('');
    }
    
    async function fetchFlights(filters = {}) {
        let url = '/api/flights?';
        Object.keys(filters).forEach(k => {
            if(filters[k]) url += encodeURIComponent(k) + '=' + encodeURIComponent(filters[k]) + '&';
        });
        const res = await fetch(url);
        const data = await res.json();
        renderFlights(data);
    }
 
    document.getElementById('searchFlightForm').addEventListener('submit', function (e) {
        e.preventDefault();
        fetchFlights({
            origin: document.getElementById('origin').value,
            destination: document.getElementById('destination').value,
            departureDate: document.getElementById('departureDate').value
        });
    });

    // Load all flights on landing
    fetchFlights();
});
