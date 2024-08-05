function loadServerStatus() {
    fetch('/admin/server-status')
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById('server-status-body');
            tableBody.innerHTML = ''; // Clear the table before appending

            data.forEach(server => {
                const statusClass = getStatusClass(server.statusName); 
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${server.domain}</td>
                    <td>${server.ipAddress}</td>
                    <td class="${statusClass}">${server.statusName}</td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error('Error fetching server status:', error));
}

function getStatusClass(status) {
    switch (status.toLowerCase()) {
        case 'running':
            return 'status-running';
        case 'off':
            return 'status-off';
        case 'critical':
            return 'status-critical';
        default:
            return '';  // Return an empty string if no class is applicable
    }
}
