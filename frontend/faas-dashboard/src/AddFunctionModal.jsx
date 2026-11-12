import { useState } from 'react';
import { TextInput, Button, Text } from '@tremor/react';

export default function AddFunctionModal({ isOpen, onClose, onFunctionAdded }) {
  const [name, setName] = useState('');
  const [dockerImage, setDockerImage] = useState('');
  const [runtime, setRuntime] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);

    const newFunction = {
      name,
      dockerImage,
      runtime
    };

    try {
      const response = await fetch('http://localhost:8080/api/function', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newFunction),
      });

      if (response.ok) {
        onFunctionAdded();
        handleClose();
      } else {
        const errText = await response.text();
        setError(`Erro ao salvar: ${errText}`);
      }
    } catch (err) {
      setError('Erro de rede ao tentar conectar com o servidor.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    setName('');
    setDockerImage('');
    setRuntime('');
    setError(null);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm transition-opacity">
      <div className="bg-white dark:bg-zinc-900 rounded-xl shadow-2xl w-full max-w-md p-6 border border-gray-200 dark:border-zinc-800">
        <h2 className="text-xl font-semibold text-slate-800 dark:text-white mb-1">
          Nova Função Abstrata
        </h2>
        <Text className="dark:text-zinc-400 mb-6">
          Cadastre uma nova função no catálogo para posterior deploy.
        </Text>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-zinc-300 mb-1">
              Nome da Função *
            </label>
            <TextInput 
              placeholder="ex: face-detection-yolo" 
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-zinc-300 mb-1">
              Imagem Docker *
            </label>
            <TextInput 
              placeholder="ex: myrepo/face-detection:latest" 
              value={dockerImage}
              onChange={(e) => setDockerImage(e.target.value)}
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-zinc-300 mb-1">
              Runtime *
            </label>
            <TextInput 
              placeholder="ex: Python 3.9, Node.js 18, Go 1.21" 
              value={runtime}
              onChange={(e) => setRuntime(e.target.value)}
              required
            />
          </div>

          {error && (
            <div className="p-3 bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 text-sm rounded-lg border border-red-200 dark:border-red-800">
              {error}
            </div>
          )}

          <div className="flex justify-end gap-3 mt-8">
            <Button 
              type="button" 
              variant="light" 
              color="slate" 
              onClick={handleClose}
              disabled={isSubmitting}
            >
              Cancelar
            </Button>
            <Button 
              type="submit" 
              color="emerald" 
              disabled={isSubmitting}
              className="bg-emerald-600 hover:bg-emerald-700 dark:bg-emerald-500 dark:hover:bg-emerald-400 dark:text-zinc-900 border-transparent"
            >
              {isSubmitting ? 'Salvando...' : 'Salvar Função'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}