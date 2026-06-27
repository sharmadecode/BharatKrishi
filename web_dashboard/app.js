// Firebase Configuration — loaded from firebase-config.js (see firebase-config.example.js)

function showStatus(message, isError) {
    const el = document.getElementById('status-msg');
    if (!el) return;
    el.textContent = message;
    el.style.color = isError ? '#ff4444' : '#00C851';
    el.style.display = 'block';
}

function escapeHTML(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

if (typeof firebaseConfig === 'undefined') {
    document.addEventListener('DOMContentLoaded', function () {
        showStatus('Missing firebase-config.js — copy firebase-config.example.js', true);
        document.getElementById('alert-count').innerText = "Error";
        document.getElementById('scan-count').innerText = "Error";
    });
} else if (window.location.protocol === 'file:') {
    document.addEventListener('DOMContentLoaded', function () {
        showStatus('⚠️ Cannot run from file://. Use a local server: python -m http.server 8080', true);
        document.getElementById('alert-count').innerText = "Error";
        document.getElementById('scan-count').innerText = "Error";
    });
} else {
    // Initialize Firebase
    try {
        firebase.initializeApp(firebaseConfig);
        const db = firebase.firestore();
        
        // Initialize Map centered on India
        const map = L.map('map').setView([20.5937, 78.9629], 5);

        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(map);

        const markersLayer = L.layerGroup().addTo(map);

        function addMarker(lat, lng, disease, confidence) {
            let color = 'yellow';
            let fillColor = '#ffbb33';
            const radius = 100; // Fixed 100 meter radius
            let riskLabel = "Critical";

            if (disease === 'Healthy') {
                color = 'green';
                fillColor = '#00C851';
                riskLabel = "Safe";
            } else if (confidence < 0.7 || disease === 'Unknown') {
                color = 'orange';
                fillColor = '#FF8800';
                riskLabel = "Potential Risk";
            } else {
                color = 'red';
                fillColor = '#ff4444';
                riskLabel = "Critical";
            }

            L.circle([lat, lng], {
                color: color,
                fillColor: fillColor,
                fillOpacity: 0.4,
                radius: radius
            }).addTo(markersLayer);

            const escapedDisease = escapeHTML(disease);
            const escapedRiskLabel = escapeHTML(riskLabel);

            L.marker([lat, lng]).addTo(markersLayer)
                .bindPopup(`
                    <b>${escapedDisease}</b><br>
                    <b>Status:</b> ${escapedRiskLabel}<br>
                    Confidence: ${(confidence * 100).toFixed(1)}%<br>
                    Lat: ${lat.toFixed(4)}, Lng: ${lng.toFixed(4)}
                `);
        }

        function setupRealtimeListener() {
            console.log("Listening for real-time updates...");
            document.getElementById('alert-count').innerText = "Live...";
            showStatus('🔄 Syncing...', false);

            db.collection("detections")
                .orderBy("timestamp", "desc")
                .limit(100)
                .onSnapshot((querySnapshot) => {
                    markersLayer.clearLayers();

                    let activeAlerts = 0;
                    let totalScans = 0;

                    querySnapshot.forEach((doc) => {
                        const data = doc.data();
                        if (data.latitude && data.longitude) {
                            addMarker(data.latitude, data.longitude, data.diseaseName, data.confidence);
                            totalScans++;
                            if (data.diseaseName !== 'Healthy' && data.diseaseName !== 'Unknown') {
                                activeAlerts++;
                            }
                        }
                    });

                    document.getElementById('alert-count').innerText = activeAlerts + " Critical";
                    document.getElementById('scan-count').innerText = totalScans + " Total";
                    showStatus('✅ Live — ' + totalScans + ' scans loaded', false);
                    console.log("Data loaded: " + totalScans + " scans, " + activeAlerts + " alerts");
                }, (error) => {
                    console.error("Firestore Error: ", error);
                    if (error.code === 'permission-denied') {
                        showStatus('🔒 Firestore rules deny read access. Update rules in Firebase Console.', true);
                    } else if (error.code === 'unavailable') {
                        showStatus('📡 Firestore unreachable. Check internet connection.', true);
                    } else {
                        showStatus('❌ Error: ' + error.message, true);
                    }
                    document.getElementById('alert-count').innerText = "Error";
                    document.getElementById('scan-count').innerText = "Error";
                });
        }

        function clearLocalMarkers() {
            markersLayer.clearLayers();
            document.getElementById('alert-count').innerText = "0 Critical";
            document.getElementById('scan-count').innerText = "0 Total";
            alert("Map cleared locally. Firestore data is unchanged.");
        }

        window.refreshData = setupRealtimeListener;
        window.clearLocalMarkers = clearLocalMarkers;

        // Initial Load
        setupRealtimeListener();

    } catch (e) {
        console.error("Firebase Init Error:", e);
        showStatus('❌ Firebase init failed: ' + e.message, true);
    }
}
