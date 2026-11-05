const API_URL = "http://localhost:8080/dsaApp/drones";

async function fetchDrones() {
    const container = document.getElementById('drones');

    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }

        const data = await response.json();
        container.innerHTML = "";

        data.forEach(drone => {
            const card = document.createElement('div');
            card.classList.add('track-card');

            const title = document.createElement('h3');
            title.textContent = drone.name;
            const info = document.createElement('p');
            info.textContent = `${drone.manufacturer || '-'} ${drone.model || ''} · ${drone.flightHours} h de vuelo`
                + (drone.inMaintenance ? ' · en mantenimiento' : '');
            const ref = document.createElement('small');
            ref.textContent = `Ref: ${drone.id}`;

            card.append(title, info, ref);
            container.appendChild(card);
        });
    } catch (error) {
        console.error("Error al cargar drones:", error);
        container.innerHTML = `<p style="color: red; text-align: center;">No se pudo conectar con la API en ${API_URL}</p>`;
    }
}

fetchDrones();
