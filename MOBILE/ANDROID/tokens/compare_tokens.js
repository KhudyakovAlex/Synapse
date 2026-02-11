const fs = require('fs');

// Read Pixso tokens
const pixsoTokens = JSON.parse(fs.readFileSync('C:/Users/khudyakov/.cursor/projects/d-Git-Synapse-MOBILE-ANDROID/agent-tools/5d495f41-f0ec-4e53-b124-d3e945dc3bbd.txt', 'utf8'));

// Read current mapping
const currentMap = JSON.parse(fs.readFileSync('d:/Git/Synapse/MOBILE/ANDROID/tokens/pixso_tokens_map.json', 'utf8'));

// Create Pixso tokens map by id
const pixsoMap = new Map();
const softDeletedMap = new Map();

pixsoTokens.forEach(token => {
  if (token.isSoftDeleted) {
    softDeletedMap.set(token.id, token);
  } else if (token.isVisible) {
    pixsoMap.set(token.id, token);
  }
});

// Arrays for results
const newTokens = [];
const removedTokens = [];
const changedTokens = [];

// Helper to format color value
function formatColorValue(colorObj) {
  if (!colorObj) return null;
  const r = Math.round(colorObj.r);
  const g = Math.round(colorObj.g);
  const b = Math.round(colorObj.b);
  const a = Math.round(colorObj.a);
  const hexA = a.toString(16).padStart(2, '0').toUpperCase();
  const hexR = r.toString(16).padStart(2, '0').toUpperCase();
  const hexG = g.toString(16).padStart(2, '0').toUpperCase();
  const hexB = b.toString(16).padStart(2, '0').toUpperCase();
  return `0x${hexA}${hexR}${hexG}${hexB}`;
}

// Helper to get token value from Pixso
function getPixsoValue(token) {
  const modeValue = token.modeValues['2:1'];
  if (!modeValue) return null;
  
  if (token.resolvedType === 4) { // Color
    return formatColorValue(modeValue.value);
  } else if (token.resolvedType === 1) { // Number
    return modeValue.value;
  } else if (token.resolvedType === 2) { // String
    return modeValue.value;
  }
  return null;
}

// Find NEW tokens (in Pixso, not in mapping)
pixsoMap.forEach((token, id) => {
  if (!currentMap[id]) {
    const value = getPixsoValue(token);
    newTokens.push({
      id: token.id,
      name: token.name,
      value: value,
      type: token.resolvedType === 4 ? 'color' : (token.resolvedType === 1 ? 'number' : 'string'),
      key: token.key
    });
  }
});

// Find REMOVED tokens (in mapping, but deleted or missing in Pixso)
Object.keys(currentMap).forEach(id => {
  const mappedToken = currentMap[id];
  const pixsoToken = pixsoMap.get(id);
  
  if (!pixsoToken) {
    // Check if it was soft-deleted in Pixso
    const deletedToken = softDeletedMap.get(id);
    removedTokens.push({
      id: id,
      name: mappedToken.pixsoName,
      kotlinName: mappedToken.kotlinName,
      wasDeleted: !!deletedToken,
      type: mappedToken.type
    });
  }
});

// Find CHANGED tokens
Object.keys(currentMap).forEach(id => {
  const mappedToken = currentMap[id];
  const pixsoToken = pixsoMap.get(id);
  
  if (pixsoToken) {
    const pixsoValue = getPixsoValue(pixsoToken);
    const mappedValue = mappedToken.value;
    
    // Compare values for non-alias tokens
    if (!mappedToken.isAlias && pixsoValue !== null && mappedValue !== pixsoValue) {
      changedTokens.push({
        id: id,
        name: mappedToken.pixsoName,
        kotlinName: mappedToken.kotlinName,
        oldValue: mappedValue,
        newValue: pixsoValue,
        type: mappedToken.type,
        changeType: 'value'
      });
    }
    
    // Check for name changes
    if (pixsoToken.name !== mappedToken.pixsoName) {
      changedTokens.push({
        id: id,
        name: mappedToken.pixsoName,
        kotlinName: mappedToken.kotlinName,
        changeType: 'name',
        oldName: mappedToken.pixsoName,
        newName: pixsoToken.name
      });
    }
  }
});

// Output results as JSON
console.log(JSON.stringify({
  new: newTokens,
  removed: removedTokens,
  changed: changedTokens,
  summary: {
    newCount: newTokens.length,
    removedCount: removedTokens.length,
    changedCount: changedTokens.length,
    totalPixsoTokens: pixsoMap.size,
    totalMappedTokens: Object.keys(currentMap).length,
    softDeletedInPixso: softDeletedMap.size
  }
}, null, 2));
