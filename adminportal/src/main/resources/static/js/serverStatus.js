function loadServerStatus() {
    fetch('/admin/server-status')
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById('server-status-body');
            tableBody.innerHTML = ''; // Clear the table before appending

            data.forEach(server => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${server.domain}</td>
                    <td>${server.ipAddress}</td>
                    <td>${server.statusName}</td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error('Error fetching server status:', error));
}
