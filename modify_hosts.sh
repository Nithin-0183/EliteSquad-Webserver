#!/bin/bash

HOSTS_FILE="/etc/hosts"
BACKUP_FILE="/etc/hosts.bak"
DEFAULT_IP="127.0.0.2"
TMP_FILE="/tmp/hosts.tmp"
DOMAIN=$2
IP=${3:-$DEFAULT_IP}

# Function to display usage
usage() {
    echo "Usage: $0 {add|remove} <domain> [IP]"
    echo "If no IP is specified for adding, the default IP 127.0.0.2 will be used."
    exit 1
}

# Function to backup the hosts file
backup_hosts() {
    cp $HOSTS_FILE $BACKUP_FILE
    echo "Backup of $HOSTS_FILE created at $BACKUP_FILE"
}

# Function to add a domain to the hosts file
add_domain() {
    # Check if the domain already exists
    if grep -q "$DOMAIN" $HOSTS_FILE; then
        echo "Domain $DOMAIN already exists. Updating IP address..."
        awk -v domain="$DOMAIN" '$2 != domain' $HOSTS_FILE > $TMP_FILE && cp $TMP_FILE $HOSTS_FILE
    fi

    # Add the new domain and IP
    echo "$IP $DOMAIN" >> $HOSTS_FILE
    echo "Added $DOMAIN with IP $IP"
}

# Function to remove a domain from the hosts file
remove_domain() {
    if grep -q "$DOMAIN" $HOSTS_FILE; then
        awk -v domain="$DOMAIN" '$2 != domain' $HOSTS_FILE > $TMP_FILE && cp $TMP_FILE $HOSTS_FILE
        echo "Removed $DOMAIN"
    else
        echo "Domain $DOMAIN not found"
    fi

    # Verify removal
    if grep -q "$DOMAIN" $HOSTS_FILE; then
        echo "Error: Failed to remove $DOMAIN from $HOSTS_FILE"
    else
        echo "Successfully removed $DOMAIN"
    fi
}

# Function to reload DNS
reload_dns() {
    if command -v systemctl >/dev/null 2>&1 && systemctl is-active systemd-resolved >/dev/null 2>&1; then
        systemctl restart systemd-resolved
        echo "Restarted systemd-resolved"
    elif command -v systemctl >/dev/null 2>&1 && systemctl is-active nscd >/dev/null 2>&1; then
        systemctl restart nscd
        echo "Restarted nscd"
    else
        echo "DNS reload not required or unsupported system"
    fi
}

# Check if sufficient arguments are provided
if [ "$#" -lt 2 ]; then
    usage
fi

# Main script
case $1 in
    add)
        backup_hosts
        add_domain
        reload_dns
        ;;
    remove)
        backup_hosts
        remove_domain
        reload_dns
        ;;
    *)
        usage
        ;;
esac
